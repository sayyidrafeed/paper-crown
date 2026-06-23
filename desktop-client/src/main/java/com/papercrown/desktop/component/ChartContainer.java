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
        chartPanel.setMinimumDrawWidth(10);
        chartPanel.setMinimumDrawHeight(10);
        chartPanel.setPreferredSize(new java.awt.Dimension(400, 250));

        swingNode = new SwingNode();
        swingNode.setContent(chartPanel);

        getChildren().add(swingNode);
    }

    public static void applyTheme(JFreeChart chart) {
        chart.setBackgroundPaint(BG_COLOR);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);
        chart.setBorderVisible(false);

        if (chart.getTitle() != null) {
            chart.getTitle().setPaint(new Color(232, 232, 240));
            chart.getTitle().setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16));
            chart.getTitle().setPadding(10, 0, 10, 0);
        }
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(BG_COLOR);
            chart.getLegend().setItemPaint(new Color(200, 200, 220));
            chart.getLegend().setItemFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
            chart.getLegend().setFrame(org.jfree.chart.block.BlockBorder.NONE);
        }

        Plot plot = chart.getPlot();
        if (plot != null) {
            plot.setBackgroundPaint(BG_COLOR);
            plot.setOutlineVisible(false);
        }

        if (plot instanceof CategoryPlot cp) {
            cp.setRangeGridlinePaint(GRID_COLOR);
            cp.setDomainGridlinesVisible(false);
            cp.setOutlineVisible(false);
            
            // Renderers styling
            org.jfree.chart.renderer.category.CategoryItemRenderer renderer = cp.getRenderer();
            if (renderer instanceof org.jfree.chart.renderer.category.BarRenderer br) {
                br.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
                br.setSeriesPaint(0, new Color(100, 149, 237)); // soft blue
                br.setShadowVisible(false);
                br.setItemMargin(0.1);
            } else if (renderer instanceof org.jfree.chart.renderer.category.LineAndShapeRenderer lsr) {
                lsr.setSeriesPaint(0, new Color(201, 168, 76)); // gold
                lsr.setSeriesStroke(0, new java.awt.BasicStroke(3.0f));
            }

            if (cp.getRangeAxis() != null) {
                cp.getRangeAxis().setTickLabelPaint(AXIS_COLOR);
                cp.getRangeAxis().setLabelPaint(new Color(180, 180, 200));
                cp.getRangeAxis().setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 11));
                cp.getRangeAxis().setLabelFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
                cp.getRangeAxis().setAxisLineVisible(false);
                cp.getRangeAxis().setTickMarksVisible(false);
            }
            if (cp.getDomainAxis() != null) {
                cp.getDomainAxis().setTickLabelPaint(AXIS_COLOR);
                cp.getDomainAxis().setLabelPaint(new Color(180, 180, 200));
                cp.getDomainAxis().setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 11));
                cp.getDomainAxis().setLabelFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
                cp.getDomainAxis().setAxisLineVisible(false);
                cp.getDomainAxis().setTickMarksVisible(false);
            }
        }

        if (plot instanceof PiePlot pp) {
            pp.setLabelPaint(new Color(232, 232, 240));
            pp.setLabelBackgroundPaint(BG_COLOR);
            pp.setLabelOutlinePaint(null);
            pp.setLabelShadowPaint(null);
            pp.setLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
            pp.setShadowPaint(null);
            pp.setOutlineVisible(false);
            pp.setSectionOutlinesVisible(false);
            pp.setInteriorGap(0.05);
        }
    }
}
