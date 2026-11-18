package com.camradar.core

import com.camradar.core.geo.LatLon
import com.camradar.core.geo.LufopParser
import com.camradar.core.geo.LufopSource
import com.camradar.core.model.CameraCategory
import com.camradar.core.model.DiscoverySource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LufopTest {
    private val sample = """[{"ID":"172755","name":"FR Radar Fixe FR 70","lat":47.7235,"lng":-0.00997,"type":"18",
        "commune":"Clermont-Créans","voie":"D323","flash":"B","azimut":"210","update":"2023-02-11 10:24:54","vitesse":"80"},
        {"ID":"x","name":"bad"}]"""

    @Test fun parsesRecords() {
        val cams = LufopParser.parse(sample, 5)
        assertEquals(1, cams.size)
        val c = cams[0]
        assertEquals("lufop:172755", c.id)
        assertEquals(DiscoverySource.LUFOP_SPEED_CAMERA, c.source)
        assertEquals(CameraCategory.TRAFFIC, c.category)
        assertEquals(47.7235, c.location!!.lat, 1e-9)
        assertTrue(c.sourceDetail!!.startsWith("80 km/h"))
    }

    @Test(expected = java.io.IOException::class) fun errorObjectThrows() { LufopParser.parse("""{"error":"bad key"}""", 0) }

    @Test fun noKeyMeansNoRequest() {
        val src = LufopSource("", { _, _ -> error("should not call") })
        assertEquals(emptyList<Any>(), src.fetch(LatLon(0.0, 0.0), 1000))
    }

    @Test fun urlHasMargin() {
        val url = LufopSource("k", { _, _ -> "[]" }).buildUrl(LatLon(45.75, 4.85), 5000)
        assertTrue(url.contains("q=45.75,4.85&m=50"))
    }
}
