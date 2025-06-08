package pl.gpwpoid.origin.ui.views.company;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.charts.model.style.SolidColor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import pl.gpwpoid.origin.repositories.DTO.TransactionDTO;
import pl.gpwpoid.origin.repositories.views.OHLCDataItem;
import pl.gpwpoid.origin.services.ChartUpdateBroadcaster;
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
    private final ChartUpdateBroadcaster broadcaster;
    private Integer companyId;
    private ChartTimeRange currentRange = ChartTimeRange.THIRTY_MINUTES;
    private Registration broadcasterRegistration;
    private DataSeries series;
    private volatile boolean isLoading = false;

    public CompanyChart(TransactionService transactionService, IPOService ipoService, ChartUpdateBroadcaster broadcaster) {
        this.transactionService = transactionService;
        this.ipoService = ipoService;
        this.broadcaster = broadcaster;
        this.series = new DataSeries("Cena akcji");

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

        SolidColor upColor = new SolidColor("#5cb85c");
        SolidColor downColor = new SolidColor("#ff3333");
        SolidColor borderColor = new SolidColor("#000");


        PlotOptionsCandlestick plotOptions = new PlotOptionsCandlestick();
        plotOptions.setUpColor(upColor);
        plotOptions.setColor(downColor);
        plotOptions.setLineColor(downColor);
        plotOptions.setUpLineColor(upColor);
        plotOptions.setShowInLegend(false);
        conf.setPlotOptions(plotOptions);

        DataSeries upSeries = new DataSeries("Wzrost");
        PlotOptionsScatter upOptions = new PlotOptionsScatter();
        Marker upMarker = upOptions.getMarker();
        upMarker.setSymbol(MarkerSymbolEnum.SQUARE);
        upMarker.setFillColor(upColor);
        upMarker.setLineWidth(0);
        upSeries.setPlotOptions(upOptions);


        DataSeries downSeries = new DataSeries("Spadek");
        PlotOptionsScatter downOptions = new PlotOptionsScatter();
        Marker downMarker = downOptions.getMarker();
        downMarker.setSymbol(MarkerSymbolEnum.SQUARE);
        downMarker.setFillColor(downColor);
        downMarker.setLineColor(borderColor);
        downMarker.setLineWidth(1);
        downSeries.setPlotOptions(downOptions);

        conf.setSeries(this.series, upSeries, downSeries);
    }


    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime();
    }


    private LocalDateTime toLocalDateTime(Number number) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(number.longValue()), SERVER_ZONE_ID);
    }

    public void loadAndRenderData(Integer companyId) {
        if (isLoading) {
            return;
        }
        isLoading = true;

        try {
            this.companyId = companyId;
            if (this.companyId == null) {
                return;
            }

            if (getUI().isPresent()) {
                registerForUpdates();
            }

            Configuration conf = candlestickChart.getConfiguration();
            LocalDateTime rawNow = LocalDateTime.now();
            LocalDateTime alignedNow = currentRange.truncate(rawNow);
            LocalDateTime startTime = currentRange.getStartTime(alignedNow);
            List<OHLCDataItem> rawDataFromDb = transactionService.getOHLCDataByCompanyId(this.companyId, startTime, rawNow);

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
            if (rawDataFromDb != null && !rawDataFromDb.isEmpty()) {
                Map<LocalDateTime, List<OHLCDataItem>> groupedByInterval = rawDataFromDb.stream()
                        .collect(Collectors.groupingBy(data -> currentRange.truncate(toLocalDateTime(data.getTimestamp()))));

                for (Map.Entry<LocalDateTime, List<OHLCDataItem>> entry : groupedByInterval.entrySet()) {
                    LocalDateTime groupTimestamp = entry.getKey();
                    List<OHLCDataItem> itemsInGroup = entry.getValue();
                    itemsInGroup.sort(Comparator.comparing(OHLCDataItem::getTimestamp));

                    BigDecimal high = itemsInGroup.stream().map(OHLCDataItem::getHigh).reduce(BigDecimal.ZERO, BigDecimal::max);
                    BigDecimal low = itemsInGroup.stream().map(OHLCDataItem::getLow).reduce(itemsInGroup.get(0).getLow(), BigDecimal::min);

                    OhlcItem aggregatedCandle = new OhlcItem();
                    aggregatedCandle.setX(groupTimestamp.atZone(SERVER_ZONE_ID).toInstant());
                    aggregatedCandle.setOpen(itemsInGroup.get(0).getOpen());
                    aggregatedCandle.setHigh(high);
                    aggregatedCandle.setLow(low);
                    aggregatedCandle.setClose(itemsInGroup.get(itemsInGroup.size() - 1).getClose());
                    candleMap.put(groupTimestamp, aggregatedCandle);
                }
            }

            BigDecimal lastRealClosePrice = anchorPrice;
            List<OhlcItem> finalCandles = new ArrayList<>();
            LocalDateTime currentTimeStep = startTime;
            while (!currentTimeStep.isAfter(alignedNow)) {
                OhlcItem candleForThisStep = candleMap.get(currentTimeStep);
                if (candleForThisStep != null) {
                    finalCandles.add(candleForThisStep);
                    lastRealClosePrice = BigDecimal.valueOf(candleForThisStep.getClose().doubleValue());
                } else {
                    OhlcItem flatCandle = new OhlcItem();
                    flatCandle.setX(currentTimeStep.atZone(SERVER_ZONE_ID).toInstant());
                    flatCandle.setOpen(lastRealClosePrice);
                    flatCandle.setHigh(lastRealClosePrice);
                    flatCandle.setLow(lastRealClosePrice);
                    flatCandle.setClose(lastRealClosePrice);
                    finalCandles.add(flatCandle);
                }
                currentTimeStep = currentTimeStep.plus(currentRange.getIntervalAmount(), currentRange.getGroupingUnit());
            }

            this.series.clear();
            for (OhlcItem item : finalCandles) {
                this.series.add(item, false, false);
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
            candlestickChart.drawChart();

        } finally {
            isLoading = false;
        }
    }

    private void handleChartUpdate(TransactionDTO transactionData) {
        if (isLoading) {
            return;
        }

        getUI().ifPresent(ui -> ui.access(() -> {
            if (series.getData().isEmpty()) {
                loadAndRenderData(companyId);
                return;
            }

            if (transactionData != null && !Objects.equals(this.companyId, transactionData.getCompanyId())) {
                return;
            }

            int lastIndex = series.getData().size() - 1;
            OhlcItem lastItem = (OhlcItem) series.get(lastIndex);
            LocalDateTime lastIntervalStart = toLocalDateTime(lastItem.getX());

            LocalDateTime eventTimestamp = (transactionData != null) ? toLocalDateTime(transactionData.getDate().getTime()) : LocalDateTime.now();
            LocalDateTime eventIntervalStart = currentRange.truncate(eventTimestamp);

            if (eventIntervalStart.isEqual(lastIntervalStart)) {
                if (transactionData != null) {
                    updateLastCandle(lastItem, transactionData);
                }
            } else {
                addMissingAndNewCandles(lastItem, eventIntervalStart, transactionData);
            }
        }));
    }

    private void addMissingAndNewCandles(OhlcItem previousItem, LocalDateTime eventIntervalStart, TransactionDTO finalUpdate) {
        LocalDateTime currentTimeStep = toLocalDateTime(previousItem.getX()).plus(currentRange.getIntervalAmount(), currentRange.getGroupingUnit());
        BigDecimal lastKnownPrice = BigDecimal.valueOf(previousItem.getClose().doubleValue());

        while (currentTimeStep.isBefore(eventIntervalStart)) {
            OhlcItem flatCandle = new OhlcItem();
            flatCandle.setX(currentTimeStep.atZone(SERVER_ZONE_ID).toInstant());
            flatCandle.setOpen(lastKnownPrice);
            flatCandle.setHigh(lastKnownPrice);
            flatCandle.setLow(lastKnownPrice);
            flatCandle.setClose(lastKnownPrice);

            series.add(flatCandle, false, true);

            currentTimeStep = currentTimeStep.plus(currentRange.getIntervalAmount(), currentRange.getGroupingUnit());
        }

        OhlcItem finalCandle = new OhlcItem();
        finalCandle.setX(eventIntervalStart.atZone(SERVER_ZONE_ID).toInstant());

        if (finalUpdate != null) {
            finalCandle.setOpen(lastKnownPrice);
            finalCandle.setHigh(lastKnownPrice.max(finalUpdate.getSharePrice()));
            finalCandle.setLow(lastKnownPrice.min(finalUpdate.getSharePrice()));
            finalCandle.setClose(finalUpdate.getSharePrice());
        } else {
            finalCandle.setOpen(lastKnownPrice);
            finalCandle.setHigh(lastKnownPrice);
            finalCandle.setLow(lastKnownPrice);
            finalCandle.setClose(lastKnownPrice);
        }
        series.add(finalCandle, false, true);

        BigDecimal newHigh = BigDecimal.valueOf(finalCandle.getHigh().doubleValue());
        BigDecimal newLow = BigDecimal.valueOf(finalCandle.getLow().doubleValue());
        adjustYAxisIfNeeded(newHigh, newLow);
        candlestickChart.drawChart();
    }

    private void updateLastCandle(OhlcItem lastItem, TransactionDTO update) {
        BigDecimal lastHigh = BigDecimal.valueOf(lastItem.getHigh().doubleValue());
        BigDecimal lastLow = BigDecimal.valueOf(lastItem.getLow().doubleValue());

        BigDecimal newHigh = lastHigh.max(update.getSharePrice());
        BigDecimal newLow = lastLow.min(update.getSharePrice());

        lastItem.setHigh(newHigh);
        lastItem.setLow(newLow);
        lastItem.setClose(update.getSharePrice());

        series.update(lastItem);
        adjustYAxisIfNeeded(newHigh, newLow);
        candlestickChart.drawChart();
    }

    private void adjustYAxisIfNeeded(BigDecimal newHigh, BigDecimal newLow) {
        YAxis yAxis = candlestickChart.getConfiguration().getyAxis();

        Number currentMaxNumber = yAxis.getMax();
        Number currentMinNumber = yAxis.getMin();

        if (currentMaxNumber == null || newHigh.doubleValue() > currentMaxNumber.doubleValue()) {
            yAxis.setMax(newHigh.doubleValue() + 1.0);
        } else {
            yAxis.setMax(currentMaxNumber.doubleValue());
        }

        if (currentMinNumber == null || newLow.doubleValue() < currentMinNumber.doubleValue()) {
            double newMin = newLow.doubleValue() - 1.0;
            yAxis.setMin(newMin < 0 ? 0 : newMin);
        } else {
            yAxis.setMin(currentMinNumber.doubleValue() < 0 ? 0 : currentMinNumber);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (companyId != null) {
            registerForUpdates();
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        unregisterForUpdates();
        super.onDetach(detachEvent);
    }

    private void registerForUpdates() {
        unregisterForUpdates();
        broadcasterRegistration = broadcaster.register(companyId, this::handleChartUpdate);
    }

    private void unregisterForUpdates() {
        if (broadcasterRegistration != null) {
            broadcasterRegistration.remove();
            broadcasterRegistration = null;
        }
    }
}