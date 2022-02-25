/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sld.layout;

import java.util.HashSet;
import java.util.Set;
import java.util.function.DoubleBinaryOperator;

import com.powsybl.sld.model.blocks.*;
import com.powsybl.sld.model.nodes.Node;

import static com.powsybl.sld.model.nodes.Node.NodeType.*;

/**
 * @author Benoit Jeanson <benoit.jeanson at rte-france.com>
 */
public class CalculateCellHeightVisitor implements BlockVisitor {
    private double blockHeight;
    private LayoutParameters layoutParameters;
    private Set<Node> encounteredNodes;

    public CalculateCellHeightVisitor(LayoutParameters layoutParameters, Set<Node> encounteredNodes) {
        this.layoutParameters = layoutParameters;
        this.encounteredNodes = encounteredNodes;
    }

    public CalculateCellHeightVisitor(LayoutParameters layoutParameters) {
        this.layoutParameters = layoutParameters;
        this.encounteredNodes = new HashSet<>();
    }

    public double getBlockHeight() {
        return blockHeight;
    }

    @Override
    public void visit(LegPrimaryBlock block) {
        blockHeight = 0;
    }

    @Override
    public void visit(FeederPrimaryBlock block) {
        blockHeight = 0;
    }

    @Override
    public void visit(BodyPrimaryBlock block) {
        // we do not consider the exact height of components as the maximum height will later be split up equally between nodes
        double componentHeight = layoutParameters.getMaxComponentHeight() + layoutParameters.getMinSpaceBetweenComponents();

        // we increment the height only if the node is not a bus node and has not been already encountered
        long nbNodes = block.getNodes().stream().filter(n -> !encounteredNodes.contains(n) && n.getType() != BUS).count();

        this.blockHeight = (nbNodes - 1) * componentHeight;
    }

    @Override
    public void visit(SerialBlock block) {
        calculateSubHeight(block, (a, b) -> a + b);
    }

    @Override
    public void visit(LegParralelBlock block) {
        calculateSubHeight(block, Math::max);
    }

    @Override
    public void visit(BodyParallelBlock block) {
        calculateSubHeight(block, Math::max);
    }

    @Override
    public void visit(UndefinedBlock block) {
        calculateSubHeight(block, Math::max);
    }

    private void calculateSubHeight(ComposedBlock parallelBlock, DoubleBinaryOperator op) {
        blockHeight = 0.;
        for (int i = 0; i < parallelBlock.getSubBlocks().size(); i++) {
            Block sub = parallelBlock.getSubBlocks().get(i);
            // Here, when the subBlocks are positioned in parallel we calculate the max height of all these subBlocks
            // when the subBlocks are serial, we calculate the sum
            CalculateCellHeightVisitor cch = new CalculateCellHeightVisitor(layoutParameters, encounteredNodes);
            sub.accept(cch);
            blockHeight = op.applyAsDouble(blockHeight, cch.blockHeight);
        }
    }
}
