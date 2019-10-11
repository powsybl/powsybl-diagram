/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.iidm.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Connectable;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class ConnectablePositionXmlSerializer<C extends Connectable<C>> implements ExtensionXmlSerializer<C, ConnectablePosition<C>> {

    @Override
    public String getExtensionName() {
        return  ConnectablePosition.NAME;
    }

    @Override
    public String getCategoryName() {
        return "network";
    }

    @Override
    public Class<? super ConnectablePosition> getExtensionClass() {
        return ConnectablePosition.class;
    }

    @Override
    public boolean hasSubElements() {
        return true;
    }

    @Override
    public InputStream getXsdAsStream() {
        return getClass().getResourceAsStream("/xsd/connectablePosition.xsd");
    }

    @Override
    public String getNamespaceUri() {
        return "http://www.itesla_project.eu/schema/iidm/ext/connectable_position/1_0";
    }

    @Override
    public String getNamespacePrefix() {
        return "cp";
    }

    private void writePosition(ConnectablePosition.Feeder feeder, Integer i, XmlWriterContext context) throws XMLStreamException {
        context.getExtensionsWriter().writeEmptyElement(getNamespaceUri(), "feeder" + (i != null ? i : ""));
        context.getExtensionsWriter().writeAttribute("name", feeder.getName());
        XmlUtil.writeInt("order", feeder.getOrder(), context.getExtensionsWriter());
        context.getExtensionsWriter().writeAttribute("direction", feeder.getDirection().name());
    }

    @Override
    public void write(ConnectablePosition connectablePosition, XmlWriterContext context) throws XMLStreamException {
        if (connectablePosition.getFeeder() != null) {
            writePosition(connectablePosition.getFeeder(), null, context);
        }
        if (connectablePosition.getFeeder1() != null) {
            writePosition(connectablePosition.getFeeder1(), 1, context);
        }
        if (connectablePosition.getFeeder2() != null) {
            writePosition(connectablePosition.getFeeder2(), 2, context);
        }
        if (connectablePosition.getFeeder3() != null) {
            writePosition(connectablePosition.getFeeder3(), 3, context);
        }
    }

    private ConnectablePosition.Feeder readPosition(XmlReaderContext context) {
        String name = context.getReader().getAttributeValue(null, "name");
        int order = XmlUtil.readIntAttribute(context.getReader(), "order");
        ConnectablePosition.Direction direction = ConnectablePosition.Direction.valueOf(context.getReader().getAttributeValue(null, "direction"));
        return new ConnectablePosition.Feeder(name, order, direction);
    }

    @Override
    public ConnectablePosition read(Connectable connectable, XmlReaderContext context) throws XMLStreamException {
        ConnectablePosition.Feeder[] feeder = new ConnectablePosition.Feeder[1];
        ConnectablePosition.Feeder[] feeder1 = new ConnectablePosition.Feeder[1];
        ConnectablePosition.Feeder[] feeder2 = new ConnectablePosition.Feeder[1];
        ConnectablePosition.Feeder[] feeder3 = new ConnectablePosition.Feeder[1];
        XmlUtil.readUntilEndElement(getExtensionName(), context.getReader(), () -> {

            switch (context.getReader().getLocalName()) {
                case "feeder":
                    feeder[0] = readPosition(context);
                    break;

                case "feeder1":
                    feeder1[0] = readPosition(context);
                    break;

                case "feeder2":
                    feeder2[0] = readPosition(context);
                    break;

                case "feeder3":
                    feeder3[0] = readPosition(context);
                    break;

                default:
                    throw new AssertionError();
            }
        });
        return new ConnectablePosition(connectable, feeder[0], feeder1[0], feeder2[0], feeder3[0]);
    }
}
