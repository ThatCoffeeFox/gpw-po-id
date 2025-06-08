package pl.gpwpoid.origin.ui.views.company;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.gpwpoid.origin.repositories.views.OHLCDataItem;
import pl.gpwpoid.origin.services.IPOService;
import pl.gpwpoid.origin.services.TransactionService;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class CompanyChart extends VerticalLayout {
    private static final ZoneId SERVER_ZONE_ID = ZoneId.systemDefault();
    private final Chart candlestickChart = new Chart(ChartType.CANDLESTICK);
    private final H3 chartHeader = new H3("Historia cen akcji");
    private final TransactionService transactionService;
    private final IPOService ipoService;
    private Integer companyId;
    private ChartTimeRange currentRange = ChartTimeRange.THIRTY_MINUTES;

    public CompanyChart(TransactionService transactionService, IPOService ipoService) {
        this.transactionService = transactionService;
        this.ipoService = ipoService;
        configureChart();

        HorizontalLayout headerWithControls = new HorizontalLayout(chartHeader, createRangeSelectorButtons());
        headerWithControls.setAlignItems(Alignment.BASELINE);
        headerWithControls.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerWithControls.setWidth("100%");

        add(headerWithControls, candlestickChart);
        setWidth("100%");
        setAlignItems(Alignment.CENTER);
        candlestickChart.setWidth("100%");
        candlestickChart.setHeight("400px");
    }

    private HorizontalLayout createRangeSelectorButtons() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        for (ChartTimeRange range : ChartTimeRange.values()) {
            Button rangeButton = new Button(range.getLabel());
            rangeButton.addClickListener(event -> {
                this.currentRange = range;

                loadAndRenderData(this.companyId);
            });

            buttonLayout.add(rangeButton);
        }
        return buttonLayout;
    }

    private void configureChart() {
        Configuration conf = candlestickChart.getConfiguration();

        Time time = new Time();
        time.setUseUTC(false);
        conf.setTime(time);

        conf.setTitle((String) null);

        XAxis xAxis = new XAxis();
        xAxis.setType(AxisType.DATETIME);
        conf.addxAxis(xAxis);

        YAxis yAxis = new YAxis();
        yAxis.setTitle("Cena");
        conf.addyAxis(yAxis);

        Tooltip tooltip = new Tooltip();
        tooltip.setDateTimeLabelFormats(new DateTimeLabelFormats());
        tooltip.setPointFormat("<span style=\"color: {point.color}\">●</span> <b> {series.name}</b><br/>" +
                "Otwarcie: {point.open}<br/>" +
                "Najwyższa: {point.high}<br/>" +
                "Najniższa: {point.low}<br/>" +
                "Zamknięcie: {point.close}<br/>");
        conf.setTooltip(tooltip);

        conf.setPlotOptions(new PlotOptionsCandlestick());
    }


    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime();
    }


    private LocalDateTime toLocalDateTime(Number number) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(number.longValue()), SERVER_ZONE_ID);
    }

    public void loadAndRenderData(Integer companyId) {
        this.companyId = companyId;
        if (this.companyId == null) {
            return;
        }

        Configuration conf = candlestickChart.getConfiguration();
        LocalDateTime rawNow = LocalDateTime.now();
        LocalDateTime alignedNow = currentRange.truncate(rawNow);
        LocalDateTime startTime = currentRange.getStartTime(alignedNow);

        List<OHLCDataItem> rawDataFromDb = transactionService.getOHLCDataByCompanyId(
                this.companyId,
                startTime,
                rawNow
        );

        BigDecimal anchorPrice;
        Optional<BigDecimal> previousCloseOpt = transactionService.findLastSharePriceBeforeDate(this.companyId, startTime);

        if (previousCloseOpt.isPresent()) {
            anchorPrice = previousCloseOpt.get();
        } else {
            Optional<BigDecimal> ipoPriceOpt = ipoService.getIpoPriceByCompanyId(this.companyId);
            if (ipoPriceOpt.isPresent()) {
                anchorPrice = ipoPriceOpt.get();
            } else {
                if (!rawDataFromDb.isEmpty()) {
                    anchorPrice = rawDataFromDb.get(0).getOpen();
                } else {
                    BigDecimal currentPrice = transactionService.getShareValueByCompanyId(companyId);
                    anchorPrice = (currentPrice != null) ? currentPrice : BigDecimal.ZERO;
                }
            }
        }

        chartHeader.setText("Historia cen akcji");
        Map<LocalDateTime, OhlcItem> candleMap = new TreeMap<>();
        if (rawDataFromDb != null) {
            Map<LocalDateTime, List<OHLCDataItem>> groupedByInterval = rawDataFromDb.stream()
                    .collect(Collectors.groupingBy(data -> currentRange.truncate(toLocalDateTime(data.getTimestamp()))));

            for (Map.Entry<LocalDateTime, List<OHLCDataItem>> entry : groupedByInterval.entrySet()) {
                LocalDateTime groupTimestamp = entry.getKey();
                List<OHLCDataItem> itemsInGroup = entry.getValue();
                itemsInGroup.sort(Comparator.comparing(OHLCDataItem::getTimestamp));

                OhlcItem aggregatedCandle = new OhlcItem();
                aggregatedCandle.setX(groupTimestamp.atZone(SERVER_ZONE_ID).toInstant());
                aggregatedCandle.setOpen(itemsInGroup.get(0).getOpen());
                aggregatedCandle.setHigh(itemsInGroup.stream().map(OHLCDataItem::getHigh).mapToDouble(Number::doubleValue).max().orElse(0));
                aggregatedCandle.setLow(itemsInGroup.stream().map(OHLCDataItem::getLow).mapToDouble(Number::doubleValue).min().orElse(0));
                aggregatedCandle.setClose(itemsInGroup.get(itemsInGroup.size() - 1).getClose());
                candleMap.put(groupTimestamp, aggregatedCandle);
            }
        }

        OhlcItem lastKnownCandle = new OhlcItem();
        lastKnownCandle.setClose(anchorPrice);

        List<OhlcItem> finalCandles = new ArrayList<>();
        LocalDateTime currentTimeStep = startTime;

        while (!currentTimeStep.isAfter(alignedNow)) {
            OhlcItem candleForThisStep = candleMap.get(currentTimeStep);

            if (candleForThisStep != null) {
                finalCandles.add(candleForThisStep);
                lastKnownCandle = candleForThisStep;
            } else {
                OhlcItem flatCandle = new OhlcItem();
                flatCandle.setX(currentTimeStep.atZone(SERVER_ZONE_ID).toInstant());

                Number lastClose = lastKnownCandle.getClose();
                flatCandle.setOpen(lastClose);
                flatCandle.setHigh(lastClose);
                flatCandle.setLow(lastClose);
                flatCandle.setClose(lastClose);
                finalCandles.add(flatCandle);

                lastKnownCandle = flatCandle;
            }

            currentTimeStep = currentTimeStep.plus(currentRange.getIntervalAmount(), currentRange.getGroupingUnit());
        }

        if (!finalCandles.isEmpty()) {
            double minLow = finalCandles.stream()
                    .mapToDouble(item -> item.getLow().doubleValue())
                    .min()
                    .orElse(0.0);

            double maxHigh = finalCandles.stream()
                    .mapToDouble(item -> item.getHigh().doubleValue())
                    .max()
                    .orElse(100.0);

            YAxis yAxis = conf.getyAxis();

            yAxis.setMin(minLow - 1.0 < 0 ? 0.0 : minLow - 1.0);
            yAxis.setMax(maxHigh + 1.0);
        } else {
            YAxis yAxis = conf.getyAxis();
            yAxis.setMin(0);
            yAxis.setMax(1);
        }

        DataSeries newDataSeries = new DataSeries("Cena Akcji");
        for (OhlcItem item : finalCandles) {
            newDataSeries.add(item);
        }

        conf.setSeries(newDataSeries);
        candlestickChart.drawChart();
    }
}