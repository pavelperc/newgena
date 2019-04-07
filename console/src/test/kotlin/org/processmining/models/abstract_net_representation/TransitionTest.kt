package org.processmining.models.abstract_net_representation

import com.pavelperc.newgena.utils.common.emptyMarking
import com.pavelperc.newgena.utils.common.markingOf
import org.junit.Test

import org.junit.Assert.*
import org.processmining.models.descriptions.SimpleGenerationDescription
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet
import org.processmining.models.graphbased.directed.petrinet.impl.ResetInhibitorNetImpl
import org.processmining.utils.helpers.SimpleGenerationHelper

class TransitionTest {
    
    @Test
    fun checkAvailability() {
        
        val petrinet: ResetInhibitorNet = ResetInhibitorNetImpl("resetInhibitorNet")
        
        val a = petrinet.addTransition("A")
        
        val p1 = petrinet.addPlace("p1")
        val p2 = petrinet.addPlace("p2")
        val p3 = petrinet.addPlace("p3")
        val p4 = petrinet.addPlace("p4")
        
        petrinet.addResetArc(p1, a)
        petrinet.addInhibitorArc(p2, a)
        petrinet.addArc(p3, a, 3)
        
        petrinet.addArc(a, p4, 4)
    
        // p1 -->>\
        // p2 ----o|- A -4- p4
        // p3 -3-/
        
        val simpleGenerationHelper = SimpleGenerationHelper
                .createHelper(
                        petrinet,
                        markingOf(p1 to 2),
                        emptyMarking(),
                        SimpleGenerationDescription()
                        )
        
        val trans = simpleGenerationHelper.allModelMovables[0]
        
        val in1 = trans.inputPlaces[0]
        
        
        
        
        
    }
}