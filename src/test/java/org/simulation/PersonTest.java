package org.simulation;

import org.junit.jupiter.api.Test;
import States.EmergencyState;
import States.IStates;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PersonTest {

    @Test
    void testMedicStartsWithEmergencyState() {
        Person medic = new Person(Person.PersonType.MEDIC);
        assertNotNull(medic.getCurrentState());
        assertInstanceOf(EmergencyState.class, medic.getCurrentState());
    }

    @Test
    void testSecurityStartsWithEmergencyState() {
        Person security = new Person(Person.PersonType.SECURITY);
        assertNotNull(security.getCurrentState());
        assertInstanceOf(EmergencyState.class, security.getCurrentState());
    }
}

