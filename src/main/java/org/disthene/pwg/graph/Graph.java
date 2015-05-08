package org.disthene.pwg.graph;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by aivanov on 5/6/15.
 *
 */
public class Graph {
    private static final String IMAGE_FORMAT = "png";
    private static final int MARGIN = 40;
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color MAJOR_LINE_COLOR = new Color(114, 114, 114);
    private static final Color MINOR_LINE_COLOR = new Color(44, 44, 44);
    private static final Color LABEL_COLOR = new Color(255, 255, 255);

    private int width = 1;
    private int height = 1;
    private BufferedImage image;
    private  Graphics2D g2d;

    private long startTime;
    private long endTime;

    public Graph(int width, int height) {
        this.width = width;
        this.height = height;

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();

        // stub
        DateTime dt = new DateTime(2014, 9, 30, 11, 30, 0, 0);
        startTime = dt.getMillis() / 1000;
        dt = new DateTime(2015, 5, 2, 12, 0, 0, 0);
        endTime = dt.getMillis() / 1000;
    }

    private void drawGrid() {
        // draw minor vertical grid lines
        float secondsPerPixel = (endTime - startTime) / (width - getLeftMargin() - getRightMargin());
        long timeRange = endTime - startTime;
        XAxisConfig config = XAxisConfigProvider.getXAxisConfig(secondsPerPixel, timeRange);
        System.out.println(config);

        double xScaleFactor = (double) width / (double) timeRange;

        long dt = 0;
        long delta = 1;
        long majorDt = 0;
        long majorDelta = 1;
        long labelDt = 0;
        long labelDelta = 1;
        if (config.getMinorGridUnit() == XAxisConfigProvider.SEC) {
            dt = startTime - (long)(startTime % config.getMinorGridStep());
            delta = (long) config.getMinorGridStep();
        } else if (config.getMinorGridUnit() == XAxisConfigProvider.MIN) {
            DateTime tdt = new DateTime(startTime * 1000);
            dt = tdt.withSecondOfMinute(0).withMinuteOfHour((int) (tdt.getMinuteOfHour() - (tdt.getMinuteOfHour() % config.getMinorGridStep()))).getMillis() / 1000;
            delta = (long) config.getMinorGridStep() * 60;
        } else if (config.getMinorGridUnit() == XAxisConfigProvider.HOUR) {
            DateTime tdt = new DateTime(startTime * 1000);
            dt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay((int) (tdt.getHourOfDay() - (tdt.getHourOfDay() % config.getMinorGridStep()))).getMillis() / 1000;
            delta = (long) config.getMinorGridStep() * 60 * 60;
        } else if (config.getMinorGridUnit() == XAxisConfigProvider.DAY) {
            DateTime tdt = new DateTime(startTime * 1000);
            dt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay(0).getMillis() / 1000;
            delta = (long) config.getMinorGridStep() * 60 * 60 * 24;
        }

        if (config.getMajorGridUnit() == XAxisConfigProvider.SEC) {
            majorDt = startTime - (long)(startTime % config.getMajorGridStep());
            majorDelta = (long) config.getMajorGridStep();
        } else if (config.getMajorGridUnit() == XAxisConfigProvider.MIN) {
            DateTime tdt = new DateTime(startTime * 1000);
            majorDt = tdt.withSecondOfMinute(0).withMinuteOfHour((int) (tdt.getMinuteOfHour() - (tdt.getMinuteOfHour() % config.getMajorGridStep()))).getMillis() / 1000;
            majorDelta = (long) config.getMajorGridStep() * 60;
        } else if (config.getMajorGridUnit() == XAxisConfigProvider.HOUR) {
            DateTime tdt = new DateTime(startTime * 1000);
            majorDt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay((int) (tdt.getHourOfDay() - (tdt.getHourOfDay() % config.getMajorGridStep()))).getMillis() / 1000;
            majorDelta = (long) config.getMajorGridStep() * 60 * 60;
        } else if (config.getMajorGridUnit() == XAxisConfigProvider.DAY) {
            DateTime tdt = new DateTime(startTime * 1000);
            majorDt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay(0).getMillis() / 1000;
            majorDelta = (long) config.getMajorGridStep() * 60 * 60 * 24;
        }

        if (config.getLabelUnit() == XAxisConfigProvider.SEC) {
            labelDt = startTime - (long)(startTime % config.getLabelStep());
            labelDelta = (long) config.getLabelStep();
        } else if (config.getLabelUnit() == XAxisConfigProvider.MIN) {
            DateTime tdt = new DateTime(startTime * 1000);
            labelDt = tdt.withSecondOfMinute(0).withMinuteOfHour((int) (tdt.getMinuteOfHour() - (tdt.getMinuteOfHour() % config.getLabelStep()))).getMillis() / 1000;
            labelDelta = (long) config.getLabelStep() * 60;
        } else if (config.getLabelUnit() == XAxisConfigProvider.HOUR) {
            DateTime tdt = new DateTime(startTime * 1000);
            labelDt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay((int) (tdt.getHourOfDay() - (tdt.getHourOfDay() % config.getLabelStep()))).getMillis() / 1000;
            labelDelta = (long) config.getLabelStep() * 60 * 60;
        } else if (config.getLabelUnit() == XAxisConfigProvider.DAY) {
            DateTime tdt = new DateTime(startTime * 1000);
            labelDt = tdt.withSecondOfMinute(0).withMinuteOfHour(0).withHourOfDay(0).getMillis() / 1000;
            labelDelta = (long) config.getLabelStep() * 60 * 60 * 24;
        }

        while (dt < startTime) dt += delta;
        while (majorDt < startTime) majorDt += majorDelta;
        while (labelDt < startTime) labelDt += labelDelta;

        g2d.setPaint(MINOR_LINE_COLOR);
        g2d.setStroke(new BasicStroke(0f));

        while (dt < endTime) {
            int x = (int) (getLeftMargin() + (dt - startTime) * xScaleFactor);

            if (x < (width - getRightMargin())) {
                g2d.drawLine(x, getTopMargin(), x, height - getBottomMargin());
            }

            dt += delta;
        }

        g2d.setPaint(MAJOR_LINE_COLOR);
        g2d.setStroke(new BasicStroke(1f));

        while (majorDt < endTime) {
            int x = (int) (getLeftMargin() + (majorDt - startTime) * xScaleFactor);

            if (x < (width - getRightMargin())) {
                g2d.drawLine(x, getTopMargin(), x, height - getBottomMargin());
            }

            majorDt += majorDelta;
        }

        g2d.drawLine(getLeftMargin(), getTopMargin(), getLeftMargin(), height - getBottomMargin());
        g2d.drawLine(getLeftMargin(), height - getBottomMargin(), width - getRightMargin(), height - getBottomMargin());
        g2d.drawLine(width - getRightMargin(), getTopMargin(), width - getRightMargin(), height - getBottomMargin());
        g2d.drawLine(getLeftMargin(), getTopMargin(), width - getRightMargin(), getTopMargin());

        g2d.setPaint(LABEL_COLOR);
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
        g2d.setFont(font);
        FontMetrics fontMetrics = g2d.getFontMetrics(font);
        int fontHeight = fontMetrics.getHeight();

        while (labelDt < endTime) {
            String label =  (new DateTime(labelDt * 1000)).toString(DateTimeFormat.forPattern(config.getFormat()));
            int labelWidth = fontMetrics.stringWidth(label);
            int x = (int) (getLeftMargin() + (labelDt - startTime) * xScaleFactor - labelWidth / 2);
            int y = height - getBottomMargin() + fontHeight;

            if (x < (width - getLeftMargin())) {
                g2d.drawString(label, x, y);
            }

            labelDt += labelDelta;
        }

    }

    private void draw() {
        g2d.setPaint(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, width, height);
        drawGrid();
    }

    private int getLeftMargin() {
        return MARGIN;
    }

    private int getRightMargin() {
        return MARGIN;
    }

    private int getBottomMargin() {
        return MARGIN;
    }

    private int getTopMargin() {
        return MARGIN;
    }

    /**
     * Renders image
     * @param os - stream to write to
     * @throws IOException
     */
    public void render(OutputStream os) throws IOException {
        draw();
        ImageIO.write(image, IMAGE_FORMAT, os);
    }


    // Getters and setters below
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
