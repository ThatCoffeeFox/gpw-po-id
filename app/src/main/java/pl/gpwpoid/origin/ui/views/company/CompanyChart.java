package pl.gpwpoid.origin.ui.views.company;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.stereotype.Component;
import pl.gpwpoid.origin.repositories.views.OHLCDataItem;
import pl.gpwpoid.origin.services.TransactionService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class CompanyChart extends VerticalLayout {
    private final Chart candlestickChart = new Chart(ChartType.CANDLESTICK);
    private final H3 chartHeader = new H3("Historia cen akcji");
    private final TransactionService transactionService;

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

    public void loadAndRenderData(Integer companyId) {
        if (companyId == null) {
            return;
        }

        Configuration conf = candlestickChart.getConfiguration();
        conf.setSeries(new ArrayList<>());

        List<OHLCDataItem> ohlcDataList = transactionService.getOHLCDataByCompanyId(
                companyId,
                LocalDateTime.now().minusDays(90),
                LocalDateTime.now()
        );

        if (ohlcDataList == null || ohlcDataList.isEmpty()) {
            chartHeader.setText("Historia cen akcji (brak danych)");
            candlestickChart.setVisible(false);
            candlestickChart.drawChart();
            return;
        }

        candlestickChart.setVisible(true);
        chartHeader.setText("Historia cen akcji");

        DataSeries dataSeries = new DataSeries("Cena Akcji");

        for (OHLCDataItem data : ohlcDataList) {
            if (data.getTimestamp() == null || data.getOpen() == null ||
                    data.getHigh() == null || data.getLow() == null || data.getClose() == null) {
                continue;
            }
            OhlcItem item = new OhlcItem();
            Instant instant = data.getTimestamp().toInstant();
            item.setX(instant);
            item.setOpen(data.getOpen());
            item.setHigh(data.getHigh());
            item.setLow(data.getLow());
            item.setClose(data.getClose());
            dataSeries.add(item);
        }

        if (!dataSeries.getData().isEmpty()) {
            conf.addSeries(dataSeries);
        } else {
            chartHeader.setText("Historia cen akcji (niekompletne dane)");
            candlestickChart.setVisible(false);
        }

        candlestickChart.drawChart();
    }
}