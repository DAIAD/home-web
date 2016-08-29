package eu.daiad.web.service.weather;

import org.apache.commons.lang.StringUtils;
import org.springframework.xml.xpath.NodeMapper;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AemetNodeMapper implements NodeMapper<DailyWeatherData> {

    public DailyWeatherData mapNode(Node node, int nodeNum) throws DOMException {
        Element dayElement = (Element) node;

        DailyWeatherData day = new DailyWeatherData(formatDate(dayElement.getAttribute("fecha")));

        // Get precipitation
        NodeList precipitationNodes = dayElement.getElementsByTagName("prob_precipitacion");
        for (int i = 0; i < precipitationNodes.getLength(); i++) {
            Element precipitationElement = (Element) precipitationNodes.item(i);

            String period = precipitationElement.getAttribute("periodo");

            if (!StringUtils.isBlank(period)) {
                String value = precipitationElement.getTextContent();

                if (!StringUtils.isBlank(value)) {
                    String[] tokens = StringUtils.split(period, "-");

                    switch (tokens[0]) {
                        case "00":
                        case "06":
                        case "12":
                        case "18":
                            day.getHour(day.getDate() + tokens[0]).setPrecipitation(Double.parseDouble(value));
                            break;
                    }
                }
            }

        }

        // Get wind speed and direction
        NodeList windNodes = dayElement.getElementsByTagName("viento");
        for (int i = 0; i < windNodes.getLength(); i++) {
            Element windElement = (Element) windNodes.item(i);

            String period = windElement.getAttribute("periodo");

            if (!StringUtils.isBlank(period)) {
                String[] tokens = StringUtils.split(period, "-");

                Element directionElement = (Element) windElement.getElementsByTagName("direccion").item(0);
                Element speedElement = (Element) windElement.getElementsByTagName("velocidad").item(0);

                if (!StringUtils.isBlank(directionElement.getTextContent())) {
                    switch (tokens[0]) {
                        case "00":
                        case "06":
                        case "12":
                        case "18":
                            day.getHour(day.getDate() + tokens[0]).setWindDirection(directionElement.getTextContent());
                            break;
                    }
                }

                if (!StringUtils.isBlank(speedElement.getTextContent())) {
                    switch (tokens[0]) {
                        case "00":
                        case "06":
                        case "12":
                        case "18":
                            day.getHour(day.getDate() + tokens[0]).setWindSpeed(
                                            Double.parseDouble(speedElement.getTextContent()));
                            break;
                    }
                }
            }
        }

        // Get temperature
        NodeList temperatureNodes = dayElement.getElementsByTagName("temperatura");
        for (int i = 0; i < temperatureNodes.getLength(); i++) {
            Element temperatureElement = (Element) temperatureNodes.item(i);

            // Get minimum / maximum values
            Element minElement = (Element) temperatureElement.getElementsByTagName("minima").item(0);
            Element maxElement = (Element) temperatureElement.getElementsByTagName("maxima").item(0);

            if (!StringUtils.isBlank(minElement.getTextContent())) {
                day.setMinTemperature(Double.parseDouble(minElement.getTextContent()));
            }
            if (!StringUtils.isBlank(maxElement.getTextContent())) {
                day.setMaxTemperature(Double.parseDouble(maxElement.getTextContent()));
            }

            // Get value per hour
            NodeList datumElements = temperatureElement.getElementsByTagName("dato");

            for (int d = 0; d < datumElements.getLength(); d++) {
                Element datumElement = (Element) datumElements.item(d);

                String hour = datumElement.getAttribute("hora");

                if (!StringUtils.isBlank(hour)) {

                    if (!StringUtils.isBlank(datumElement.getTextContent())) {
                        switch (hour) {
                            case "00":
                            case "06":
                            case "12":
                            case "18":
                                day.getHour(day.getDate() + hour).setTemperature(
                                                Double.parseDouble(datumElement.getTextContent()));
                                break;
                        }
                    }
                }
            }
        }

        // Get temperature feel
        NodeList temperatureFeelNodes = dayElement.getElementsByTagName("sens_termica");
        for (int i = 0; i < temperatureFeelNodes.getLength(); i++) {
            Element temperatureFeelElement = (Element) temperatureFeelNodes.item(i);

            // Get minimum / maximum values
            Element minElement = (Element) temperatureFeelElement.getElementsByTagName("minima").item(0);
            Element maxElement = (Element) temperatureFeelElement.getElementsByTagName("maxima").item(0);

            if (!StringUtils.isBlank(minElement.getTextContent())) {
                day.setMinTemperatureFeel(Double.parseDouble(minElement.getTextContent()));
            }
            if (!StringUtils.isBlank(maxElement.getTextContent())) {
                day.setMaxTemperatureFeel(Double.parseDouble(maxElement.getTextContent()));
            }

            // Get value per hour
            NodeList datumElements = temperatureFeelElement.getElementsByTagName("dato");

            for (int d = 0; d < datumElements.getLength(); d++) {
                Element datumElement = (Element) datumElements.item(d);

                String hour = datumElement.getAttribute("hora");

                if (!StringUtils.isBlank(hour)) {

                    if (!StringUtils.isBlank(datumElement.getTextContent())) {
                        switch (hour) {
                            case "00":
                            case "06":
                            case "12":
                            case "18":
                                day.getHour(day.getDate() + hour).setTemperatureFeel(
                                                Double.parseDouble(datumElement.getTextContent()));
                                break;
                        }
                    }
                }
            }
        }

        // Get humidity
        NodeList humidityNodes = dayElement.getElementsByTagName("humedad_relativa");
        for (int i = 0; i < humidityNodes.getLength(); i++) {
            Element humidityElement = (Element) humidityNodes.item(i);

            // Get minimum / maximum values
            Element minElement = (Element) humidityElement.getElementsByTagName("minima").item(0);
            Element maxElement = (Element) humidityElement.getElementsByTagName("maxima").item(0);

            if (!StringUtils.isBlank(minElement.getTextContent())) {
                day.setMinHumidity(Double.parseDouble(minElement.getTextContent()));
            }
            if (!StringUtils.isBlank(maxElement.getTextContent())) {
                day.setMaxHumidity(Double.parseDouble(maxElement.getTextContent()));
            }

            // Get value per hour
            NodeList datumElements = dayElement.getElementsByTagName("dato");

            for (int d = 0; d < datumElements.getLength(); d++) {
                Element datumElement = (Element) datumElements.item(d);

                String hour = datumElement.getAttribute("hora");

                if (!StringUtils.isBlank(hour)) {

                    if (!StringUtils.isBlank(datumElement.getTextContent())) {
                        switch (hour) {
                            case "00":
                            case "06":
                            case "12":
                            case "18":
                                day.getHour(day.getDate() + hour)
                                                .setHumidity(Double.parseDouble(datumElement.getTextContent()));
                                break;
                        }
                    }
                }
            }
        }

        return day;
    }

    private String formatDate(String value) {
        String[] tokens = StringUtils.split(value, "-");

        return StringUtils.join(tokens, "");
    }

}
