package com.pavelperc.newgena.petrinet.fastPetrinet

import com.pavelperc.newgena.petrinet.petrinetExtensions.fastPn
import com.pavelperc.newgena.petrinet.petrinetExtensions.pnmlId
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test

class FastPnTest {
    
    @Test
    fun testFastPn() {
        
        val fastPn = """
            places:
            p1 p2 p4 p5 p6 p7
            transitions:
            a(A) b c x(X) y
            arcs:
            p2-->a
            p2---------------->>b
            p1---------------->>b
            p1-->a--6-->p4--4-->b-->p7
                        p4--2-->c-->p7
            p6--o>c
            p6-->y-->p5-->x-->p6
        """.trimIndent()
        
        val petrinet = buildFastPetrinet(fastPn, "complex1")
        petrinet.fastPn shouldEqual fastPn
        
        petrinet.label shouldEqual "complex1"
        petrinet.places.map { it.pnmlId } shouldContainSame listOf("p1", "p2", "p4", "p5", "p6", "p7")
        petrinet.transitions.map { it.pnmlId } shouldContainSame listOf("a", "b", "c", "x", "y")
        petrinet.transitions.map { it.label } shouldContainSame listOf("A", "b", "c", "X", "y")
        
        fun String.pl() = petrinet.places.find { it.pnmlId == this }
        fun String.tr() = petrinet.transitions.find { it.pnmlId == this }
        
        petrinet.getArc("p1".pl(), "a".tr()).shouldNotBeNull()
        petrinet.getArc("p4".pl(), "b".tr()).shouldNotBeNull().weight shouldEqual 4
        petrinet.getArc("p4".pl(), "c".tr()).shouldNotBeNull().weight shouldEqual 2
        
        petrinet.getInhibitorArc("p6".pl(), "c".tr()).shouldNotBeNull()
        petrinet.getResetArc("p2".pl(), "b".tr()).shouldNotBeNull()
        petrinet.getResetArc("p1".pl(), "b".tr()).shouldNotBeNull()
    }
    
    
    @Test
    fun converterToFastPn() {
        val petrinet = buildFastPetrinet("""
            places:
            p1 p2 p4 p5 p6 p7
            transitions:
            a(A) b c x(X) y
            arcs:
            p2-->a
            p2---------------->>b
            p1---------------->>b
            p1-->a--6-->p4--4-->b-->p7
                        p4--2-->c-->p7
            p6 --o> c
            p6-->y-->p5-->x-->p6
        """.trimIndent(), "complex1")
        
        
        val generatedFastPn = generateFastPn(petrinet, true, true)
        println(generatedFastPn)
        val restored = buildFastPetrinet(generatedFastPn)
        
        restored.places.map { it.pnmlId } shouldContainSame petrinet.places.map { it.pnmlId }
        restored.transitions.map { it.pnmlId } shouldContainSame petrinet.transitions.map { it.pnmlId }
        restored.transitions.map { it.label } shouldContainSame petrinet.transitions.map { it.label }
        restored.edges.map { it.pnmlId } shouldContainSame petrinet.edges.map { it.pnmlId }
    }
}