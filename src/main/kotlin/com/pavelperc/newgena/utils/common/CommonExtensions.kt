package com.pavelperc.newgena.utils.common

import org.processmining.models.graphbased.directed.petrinet.elements.Place
import org.processmining.models.semantics.petrinet.Marking


fun markingOf(vararg places: Place) = Marking(places.asList())