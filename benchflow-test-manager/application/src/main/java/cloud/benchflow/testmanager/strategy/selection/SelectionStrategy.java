package cloud.benchflow.testmanager.strategy.selection;

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch) created on 2017-04-20
 */
public interface SelectionStrategy {

  enum Type {
    ONE_AT_A_TIME, RANDOM_BREAKDOWN, BOUNDARY_FIRST
  }

  SelectedExperimentBundle selectNextExperiment(String testID);

  public class SelectedExperimentBundle {
    private String experimentYamlString;
    private String deploymentDescriptorYamlString;
    private int explorationSpaceIndex;

    public SelectedExperimentBundle(String experimentYamlString,
        String deploymentDescriptorYamlString, int explorationSpaceIndex) {
      this.experimentYamlString = experimentYamlString;
      this.deploymentDescriptorYamlString = deploymentDescriptorYamlString;
      this.explorationSpaceIndex = explorationSpaceIndex;
    }

    public String getExperimentYamlString() {
      return experimentYamlString;
    }

    public String getDeploymentDescriptorYamlString() {
      return deploymentDescriptorYamlString;
    }

    public int getExplorationSpaceIndex() {
      return explorationSpaceIndex;
    }
  }
}
