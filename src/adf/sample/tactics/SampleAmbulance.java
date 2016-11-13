package adf.sample.tactics;

import adf.agent.action.Action;
import adf.agent.action.ambulance.ActionLoad;
import adf.agent.action.ambulance.ActionRescue;
import adf.agent.action.ambulance.ActionUnload;
import adf.agent.action.common.ActionMove;
import adf.agent.action.common.ActionRest;
import adf.agent.communication.MessageManager;
import adf.agent.communication.standard.bundle.information.MessageAmbulanceTeam;
import adf.agent.develop.DevelopData;
import adf.agent.info.AgentInfo;
import adf.agent.info.ScenarioInfo;
import adf.agent.info.WorldInfo;
import adf.agent.module.ModuleManager;
import adf.agent.precompute.PrecomputeData;
import adf.component.extaction.ExtAction;
import adf.component.extaction.ExtCommandAction;
import adf.component.module.complex.HumanDetector;
import adf.component.module.complex.Search;
import adf.component.tactics.TacticsAmbulance;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

import java.util.List;

public class SampleAmbulance extends TacticsAmbulance {
    private HumanDetector humanDetector;
    private Search search;

    private ExtAction actionTransport;
    private ExtAction actionExtMove;
    private ExtCommandAction actionCommandAmbulance;

    @Override
    public void initialize(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo, ModuleManager moduleManager, MessageManager messageManager, DevelopData developData) {
        worldInfo.indexClass(
                StandardEntityURN.CIVILIAN,
                StandardEntityURN.FIRE_BRIGADE,
                StandardEntityURN.POLICE_FORCE,
                StandardEntityURN.AMBULANCE_TEAM,
                StandardEntityURN.ROAD,
                StandardEntityURN.HYDRANT,
                StandardEntityURN.BUILDING,
                StandardEntityURN.REFUGE,
                StandardEntityURN.GAS_STATION,
                StandardEntityURN.AMBULANCE_CENTRE,
                StandardEntityURN.FIRE_STATION,
                StandardEntityURN.POLICE_OFFICE
        );
        // init Algorithm Module & ExtAction
        switch  (scenarioInfo.getMode()) {
            case PRECOMPUTATION_PHASE:
                this.humanDetector = moduleManager.getModule("TacticsAmbulance.HumanDetector", "adf.sample.module.complex.SampleVictimDetector");
                this.search = moduleManager.getModule("TacticsAmbulance.Search", "adf.sample.module.complex.SampleSearch");
                this.actionTransport = moduleManager.getExtAction("TacticsAmbulance.ActionTransport", "adf.sample.extaction.ActionTransport");
                this.actionExtMove = moduleManager.getExtAction("TacticsAmbulance.ActionExtMove", "adf.sample.extaction.ActionExtMove");
                this.actionCommandAmbulance = moduleManager.getExtAction("TacticsAmbulance.ActionCommandAmbulance", "adf.sample.extaction.ActionCommandAmbulance");
                break;
            case PRECOMPUTED:
                this.humanDetector = moduleManager.getModule("TacticsAmbulance.HumanDetector", "adf.sample.module.complex.SampleVictimDetector");
                this.search = moduleManager.getModule("TacticsAmbulance.Search", "adf.sample.module.complex.SampleSearch");
                this.actionTransport = moduleManager.getExtAction("TacticsAmbulance.ActionTransport", "adf.sample.extaction.ActionTransport");
                this.actionExtMove = moduleManager.getExtAction("TacticsAmbulance.ActionExtMove", "adf.sample.extaction.ActionExtMove");
                this.actionCommandAmbulance = moduleManager.getExtAction("TacticsAmbulance.ActionCommandAmbulance", "adf.sample.extaction.ActionCommandAmbulance");
                break;
            case NON_PRECOMPUTE:
                this.humanDetector = moduleManager.getModule("TacticsAmbulance.HumanDetector", "adf.sample.module.complex.SampleVictimDetector");
                this.search = moduleManager.getModule("TacticsAmbulance.Search", "adf.sample.module.complex.SampleSearch");
                this.actionTransport = moduleManager.getExtAction("TacticsAmbulance.ActionTransport", "adf.sample.extaction.ActionTransport");
                this.actionExtMove = moduleManager.getExtAction("TacticsAmbulance.ActionExtMove", "adf.sample.extaction.ActionExtMove");
                this.actionCommandAmbulance = moduleManager.getExtAction("TacticsAmbulance.ActionCommandAmbulance", "adf.sample.extaction.ActionCommandAmbulance");
                break;
        }
    }

    @Override
    public void precompute(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo, ModuleManager moduleManager, PrecomputeData precomputeData, DevelopData developData) {
        this.humanDetector.precompute(precomputeData);
        this.search.precompute(precomputeData);
        this.actionTransport.precompute(precomputeData);
        this.actionExtMove.precompute(precomputeData);
        this.actionCommandAmbulance.precompute(precomputeData);
    }

    @Override
    public void resume(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo, ModuleManager moduleManager, PrecomputeData precomputeData, DevelopData developData) {
        this.humanDetector.resume(precomputeData);
        this.search.resume(precomputeData);
        this.actionTransport.resume(precomputeData);
        this.actionExtMove.resume(precomputeData);
        this.actionCommandAmbulance.resume(precomputeData);
    }

    @Override
    public void preparate(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo, ModuleManager moduleManager, DevelopData developData) {
        this.humanDetector.preparate();
        this.search.preparate();
        this.actionTransport.preparate();
        this.actionExtMove.preparate();
        this.actionCommandAmbulance.preparate();
    }

    @Override
    public Action think(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo, ModuleManager moduleManager, MessageManager messageManager, DevelopData developData) {
        this.search.updateInfo(messageManager);
        this.humanDetector.updateInfo(messageManager);
        this.actionTransport.updateInfo(messageManager);
        this.actionExtMove.updateInfo(messageManager);
        this.actionCommandAmbulance.updateInfo(messageManager);

        AmbulanceTeam agent = (AmbulanceTeam)agentInfo.me();
        // command
        Action action = this.actionCommandAmbulance.calc().getAction();
        if(action != null) {
            this.sendActionMessage(messageManager, agent, action);
            return action;
        }
        // autonomous
        EntityID target = this.humanDetector.calc().getTarget();
        action = this.actionTransport.setTarget(target).calc().getAction();
        if(action != null) {
            this.sendActionMessage(messageManager, agent, action);
            return action;
        }
        target = this.search.calc().getTarget();
        action = this.actionExtMove.setTarget(target).calc().getAction();
        if(action != null) {
            this.sendActionMessage(messageManager, agent, action);
            return action;
        }

        messageManager.addMessage(
                new MessageAmbulanceTeam(true, agent, MessageAmbulanceTeam.ACTION_REST, agent.getPosition())
        );
        return new ActionRest();
    }

    private void sendActionMessage(MessageManager messageManager, AmbulanceTeam ambulance, Action action) {
        Class<? extends Action> actionClass = action.getClass();
        int actionIndex = -1;
        EntityID target = null;
        if(actionClass == ActionMove.class) {
            actionIndex = MessageAmbulanceTeam.ACTION_MOVE;
            List<EntityID> path = ((ActionMove)action).getPath();
            if(path.size() > 0) {
                target = path.get(path.size() - 1);
            }
        } else if(actionClass == ActionRescue.class) {
            actionIndex = MessageAmbulanceTeam.ACTION_RESCUE;
            target = ((ActionRescue)action).getTarget();
        } else if(actionClass == ActionLoad.class) {
            actionIndex = MessageAmbulanceTeam.ACTION_LOAD;
            target = ((ActionLoad)action).getTarget();
        } else if(actionClass == ActionUnload.class) {
            actionIndex = MessageAmbulanceTeam.ACTION_UNLOAD;
            target = ambulance.getPosition();
        } else if(actionClass == ActionRest.class) {
            actionIndex = MessageAmbulanceTeam.ACTION_REST;
            target = ambulance.getPosition();
        }
        if(actionIndex != -1) {
            messageManager.addMessage(new MessageAmbulanceTeam(true, ambulance, actionIndex, target));
        }
    }
}
