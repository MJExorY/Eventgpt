package zones;

import org.simulation.Event;
import org.simulation.FireTruck;
import sim.util.Int2D;

public class FireStation {

    private final Int2D position;
    private final Event event;
    private int dispatchedTrucks = 0;

    public FireStation(Int2D position, Event event) {
        this.position = position;
        this.event = event;
    }

    public FireTruck dispatchFireTruck(Int2D fireLocation) {
        dispatchedTrucks++;

        System.out.println("Feuerwache " + position + " entsendet Feuerwehrauto #" + dispatchedTrucks + " zu " + fireLocation);

        // Erstelle Feuerwehrauto
        FireTruck truck = new FireTruck(position, fireLocation, event);

        // Füge zum Grid hinzu
        event.grid.setObjectLocation(truck, position);

        // Schedule das Feuerwehrauto
        var stopper = event.schedule.scheduleRepeating(truck);
        truck.setStopper(stopper);

        return truck;
    }

    public Int2D getPosition() {
        return position;
    }

    public int getDispatchedTrucks() {
        return dispatchedTrucks;
    }
}