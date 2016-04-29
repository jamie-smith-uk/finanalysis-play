package finanalysis.bdd


import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(classOf[Cucumber])
@CucumberOptions(
  features = Array("test/resources")
)
class RunFinanalysisTests {

}
