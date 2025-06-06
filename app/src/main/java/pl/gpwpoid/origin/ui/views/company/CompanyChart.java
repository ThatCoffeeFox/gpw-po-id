package pl.gpwpoid.origin.ui.views.company;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import pl.gpwpoid.origin.repositories.views.OHLCDataItem;
import pl.gpwpoid.origin.services.TransactionService;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class CompanyChart extends VerticalLayout {
    private final Chart candlestickChart = new Chart(ChartType.CANDLESTICK);
    private final H3 chartHeader = new H3("Historia cen akcji");
    private final TransactionService transactionService;

    
    
    private static final ZoneId SERVER_ZONE_ID = ZoneId.systemDefault();

    public CompanyChart(TransactionService transactionService) {
        this.transactionService = transactionService;
        configureChart();
        add(chartHeader, candlestickChart);
        setWidth("100%");
        setAlignItems(Alignment.CENTER);
        candlestickChart.setWidth("100%");
        candlestickChart.setHeight("400px");
    }

    private void configureChart() {
        Configuration conf = candlestickChart.getConfiguration();

        Time time = new Time();
        time.setUseUTC(false);
        conf.setTime(time);

        conf.setTitle("Wykres świecowy");

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
        if (companyId == null) {
            return;
        }

        Configuration conf = candlestickChart.getConfiguration();
        LocalDateTime now = LocalDateTime.now();
        
        LocalDateTime thirtyMinutesAgo = now.minus(30, ChronoUnit.MINUTES);

        
        
        List<OHLCDataItem> rawDataFromDb = transactionService.getOHLCDataByCompanyId(
                companyId,
                thirtyMinutesAgo.atZone(SERVER_ZONE_ID).toLocalDateTime(),
                now.atZone(SERVER_ZONE_ID).toLocalDateTime()
        );

        if (rawDataFromDb == null || rawDataFromDb.isEmpty()) {
            chartHeader.setText("Historia cen akcji (brak danych)");
            conf.setSeries(Collections.emptyList());
            candlestickChart.drawChart();
            return;
        }

        chartHeader.setText("Historia cen akcji");

        
        Map<LocalDateTime, OhlcItem> candleMap = rawDataFromDb.stream()
                .collect(Collectors.toMap(
                        data -> toLocalDateTime(data.getTimestamp()).truncatedTo(ChronoUnit.MINUTES),
                        data -> {
                            OhlcItem item = new OhlcItem();
                            
                            item.setX(data.getTimestamp().toInstant());
                            item.setOpen(data.getOpen());
                            item.setHigh(data.getHigh());
                            item.setLow(data.getLow());
                            item.setClose(data.getClose());
                            return item;
                        },
                        (existing, replacement) -> existing
                ));

        List<OhlcItem> finalCandles = new ArrayList<>();

        OhlcItem lastAvailableCandle = rawDataFromDb.stream()
                .min(Comparator.comparing(OHLCDataItem::getTimestamp))
                .map(d -> candleMap.get(toLocalDateTime(d.getTimestamp()).truncatedTo(ChronoUnit.MINUTES)))
                .orElse(null);

        if (lastAvailableCandle == null) {
            conf.setSeries(Collections.emptyList());
            candlestickChart.drawChart();
            return;
        }

        
        LocalDateTime firstMinute = toLocalDateTime(lastAvailableCandle.getX()).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime currentMinute = firstMinute;
        LocalDateTime endMinute = now.truncatedTo(ChronoUnit.MINUTES);

        while (!currentMinute.isAfter(endMinute)) {
            if (candleMap.containsKey(currentMinute)) {
                lastAvailableCandle = candleMap.get(currentMinute);
                finalCandles.add(lastAvailableCandle);
            } else {
                OhlcItem flatCandle = new OhlcItem();
                
                flatCandle.setX(currentMinute.atZone(SERVER_ZONE_ID).toInstant());
                Number lastClose = lastAvailableCandle.getClose();
                flatCandle.setOpen(lastClose);
                flatCandle.setHigh(lastClose);
                flatCandle.setLow(lastClose);
                flatCandle.setClose(lastClose);
                finalCandles.add(flatCandle);
            }
            currentMinute = currentMinute.plus(1, ChronoUnit.MINUTES);
        }

        DataSeries newDataSeries = new DataSeries("Cena Akcji");
        for (OhlcItem item : finalCandles) {
            newDataSeries.add(item);
        }

        conf.setSeries(newDataSeries);
        candlestickChart.drawChart();
    }
}