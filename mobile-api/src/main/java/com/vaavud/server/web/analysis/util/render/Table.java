package com.vaavud.server.web.analysis.util.render;

import java.lang.reflect.Field;

public class Table {

    public static String getTable(String title, Object object) {

        StringBuilder sb = new StringBuilder(4000);

        sb.append("<h2>").append(title).append("</h2>");
        sb.append("<table>");

        if (object != null) {
            for (Field field : object.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String name = field.getName();
                Object value = null;
                try {
                    value = field.get(object);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (name == "device") {
                    addRow(sb, "Device", "...");
                } else if (name == "points") {
                    addRow(sb, "points", "...");
                } else if (name == "magneticPoints") {
                    addRow(sb, "magneticPoints", "...");
                } else if (value == null) {
                    System.out.println(name);
                } else {
                    addRow(sb, name, value.toString());
                }
            }
        }
        sb.append("</table>");

        return sb.toString();

    }

    public static void addRow(StringBuilder sb, String field, String value) {
        sb.append("<tr>");
        sb.append("<td>").append(field).append("</td>");
        sb.append("<td>").append(value).append("</td>");
        sb.append("</tr>");
    }

}
