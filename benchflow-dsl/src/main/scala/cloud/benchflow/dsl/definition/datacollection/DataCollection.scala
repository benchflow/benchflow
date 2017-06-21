package cloud.benchflow.dsl.definition.datacollection

import cloud.benchflow.dsl.definition.datacollection.clientside.ClientSideConfiguration
import cloud.benchflow.dsl.definition.datacollection.serverside.ServerSideConfiguration

/**
 * @author Jesper Findahl (jesper.findahl@usi.ch)
 *         created on 12.03.17.
 */
case class DataCollection(
  clientSide: ClientSideConfiguration,
  serverSide: Option[ServerSideConfiguration])
