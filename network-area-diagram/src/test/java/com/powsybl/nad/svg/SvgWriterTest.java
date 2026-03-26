package com.powsybl.nad.svg;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.nad.AbstractTest;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.svg.iidm.DefaultLabelProvider;
import com.powsybl.nad.svg.iidm.NominalVoltageStyleProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class SvgWriterTest extends AbstractTest {

    Network network;
    DefaultLabelProvider labelProvider;

    @BeforeEach
    void setup() {
        setLayoutParameters(new LayoutParameters());
        setSvgParameters(new SvgParameters()
            .setInsertNameDesc(true)
            .setSvgWidthAndHeightAdded(true)
            .setFixedWidth(800)
            .setEdgeStartShift(2));
        network = FourSubstationsNodeBreakerFactory.create();
    }

    @Override
    protected StyleProvider getStyleProvider(Network network) {
        return new NominalVoltageStyleProvider(network);
    }

    @Override
    protected LabelProvider getLabelProvider(Network network) {
        return labelProvider;
    }

    @Test
    void testDisconnectedLine() {
        labelProvider = new DefaultLabelProvider.Builder()
            .setInfoSideExternal(DefaultLabelProvider.EdgeInfoEnum.CURRENT)
            .setInfoSideInternal(DefaultLabelProvider.EdgeInfoEnum.EMPTY)
            .setInfoMiddleSide1(DefaultLabelProvider.EdgeInfoEnum.NAME)
            .setInfoMiddleSide2(DefaultLabelProvider.EdgeInfoEnum.EMPTY)
            .build(network, getSvgParameters());
        assertSvgEquals("/half_visible_line.svg", network, voltageLevel -> !List.of("S1VL1", "S4VL1").contains(voltageLevel.getId()));
    }

}
