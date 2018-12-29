package org.processmining.models.descriptions

import org.processmining.models.bpmn_with_data.LoggableDataObject
import org.processmining.models.bpmn_with_data.LoggableStringDataObject
import org.processmining.models.graphbased.directed.bpmn.BPMNNode
import org.processmining.models.graphbased.directed.bpmn.elements.Activity
import org.processmining.models.graphbased.directed.bpmn.elements.Gateway

/**
 * Created by Ivan on 22.02.2016.
 */
interface DescriptionWithDataObjects {
    val dataObjects: Collection<LoggableDataObject>
    
    val nodesWithOutputDataObjects: Set<BPMNNode>
    
    val exclusiveGatewaysWithInputDataObjects: Set<Gateway>
    
    val gatewaysToScriptPaths: MutableMap<Gateway, String>
    
    fun setDataObjectScriptPaths()
    
    fun getActivityScriptPath(activity: Activity): String
    
    fun getOutputDataObjects(node: BPMNNode): Collection<LoggableStringDataObject>
    
    fun getInputDataObjects(node: BPMNNode): Collection<LoggableStringDataObject>
}
