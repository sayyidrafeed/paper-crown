package com.papercrown.desktop.component;

import javafx.embed.swing.SwingNode;
import javafx.scene.layout.StackPane;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;

import javax.swing.BorderFactory;
import java.awt.Color;

public class ChartContainer extends StackPane {

    private static final Color BG_COLOR = new Color(26, 26, 36);
    private static final Color GRID_COLOR = new Color(42, 42, 56);
    private static final Color AXIS_COLOR = new Color(136, 136, 160);

    private final SwingNode swingNode;

    public ChartContainer(JFreeChart chart) {
        getStyleClass().add("chart-container");
        setMinSize(200, 150);
        setPrefSize(400, 250);

        applyTheme(chart);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(BG_COLOR);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        chartPanel.setMouseWheelEnabled(false);

        swingNode = new SwingNode();
        swingNode.setContent(chartPanel);

        getChildren().add(swingNode);
    }

    public static void applyTheme(JFreeChart chart) {
        chart.setBackgroundPaint(BG_COLOR);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        if (chart.getTitle() != null) {
            chart.getTitle().setPaint(new Color(232, 232, 240));
        }
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(BG_COLOR);
            chart.getLegend().setItemPaint(new Color(232, 232, 240));
        }

        Plot plot = chart.getPlot();
        if (plot != null) {
            plot.setBackgroundPaint(BG_COLOR);
            plot.setOutlinePaint(null);
        }

        if (plot instanceof CategoryPlot cp) {
            cp.setRangeGridlinePaint(GRID_COLOR);
            cp.setDomainGridlinePaint(GRID_COLOR);
            if (cp.getRangeAxis() != null) {
                cp.getRangeAxis().setTickLabelPaint(AXIS_COLOR);
                cp.getRangeAxis().setLabelPaint(AXIS_COLOR);
            }
            if (cp.getDomainAxis() != null) {
                cp.getDomainAxis().setTickLabelPaint(AXIS_COLOR);
                cp.getDomainAxis().setLabelPaint(AXIS_COLOR);
            }
        }

        if (plot instanceof XYPlot xyp) {
            xyp.setRangeGridlinePaint(GRID_COLOR);
            xyp.setDomainGridlinePaint(GRID_COLOR);
            if (xyp.getRangeAxis() != null) {
                xyp.getRangeAxis().setTickLabelPaint(AXIS_COLOR);
                xyp.getRangeAxis().setLabelPaint(AXIS_COLOR);
            }
            if (xyp.getDomainAxis() != null) {
                xyp.getDomainAxis().setTickLabelPaint(AXIS_COLOR);
                xyp.getDomainAxis().setLabelPaint(AXIS_COLOR);
            }
        }

        if (plot instanceof PiePlot pp) {
            pp.setLabelPaint(new Color(232, 232, 240));
            pp.setLabelBackgroundPaint(BG_COLOR);
            pp.setLabelOutlinePaint(GRID_COLOR);
        }
    }
}
