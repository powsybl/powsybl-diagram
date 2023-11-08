package com.powsybl.nad.layout;

import com.powsybl.diagram.util.forcelayout.ForceLayout.UsedParameters;
import com.powsybl.nad.model.Node;
import com.powsybl.nad.model.Point;

import java.util.HashMap;
import java.util.Map;

public class LayoutResult {
    Map<String, Point> positions = new HashMap<>();
    Map<String, com.powsybl.diagram.util.forcelayout.Point.LastStepEnergy> lastStepsEnergies = new HashMap<>();
    private UsedParameters usedParameters;

    public void add(Node node, Point p) {
        positions.put(node.getEquipmentId(), p);
    }

    public Point getPosition(String equipmentId) {
        return positions.get(equipmentId);
    }

    public void setPositions(Map<String, Point> nodePositions) {
        this.positions = nodePositions;
    }

    public void add(Node node, com.powsybl.diagram.util.forcelayout.Point.LastStepEnergy lastStepEnergy) {
        lastStepsEnergies.put(node.getEquipmentId(), lastStepEnergy);
    }

    public com.powsybl.diagram.util.forcelayout.Point.LastStepEnergy getLastStepEnergy(String equipmentId) {
        return lastStepsEnergies.get(equipmentId);
    }

    public void setLastStepsEnergies(Map<String, com.powsybl.diagram.util.forcelayout.Point.LastStepEnergy> nodeLastStepsEnergies) {
        lastStepsEnergies = nodeLastStepsEnergies;
    }

    public void setUsedParameters(UsedParameters usedParameters) {
        this.usedParameters = usedParameters;
    }

    public UsedParameters getUsedParameters() {
        return this.usedParameters;
    }
}
