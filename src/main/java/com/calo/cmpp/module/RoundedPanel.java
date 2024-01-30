package com.calo.cmpp.module;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedPanel extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        int arc = 20; // 圆角的弧度
        int width = getWidth();
        int height = getHeight();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        g2d.fill(new RoundRectangle2D.Double(0, 0, width - 1, height - 1, arc, arc));
        g2d.setColor(getForeground());
        g2d.draw(new RoundRectangle2D.Double(0, 0, width - 1, height - 1, arc, arc));
        g2d.dispose();
    }
}
