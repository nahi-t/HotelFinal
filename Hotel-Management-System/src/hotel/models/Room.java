package hotel.models;
public class Room {
    private int roomNumber;
    private String roomType;
    private String bedType;
    private int floor;
    private double pricePerNight;
    private String status;
    private boolean needsCleaning;
    private boolean needsMaintenance;
    private String amenities;
    private int maxOccupancy;
    private String viewType;
    private String description;

    // Constructor
    public Room(int roomNumber, String roomType, String bedType, int floor,
                double pricePerNight, int maxOccupancy, String viewType) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.bedType = bedType;
        this.floor = floor;
        this.pricePerNight = pricePerNight;
        this.status = "AVAILABLE";
        this.needsCleaning = false;
        this.needsMaintenance = false;
        this.maxOccupancy = maxOccupancy;
        this.viewType = viewType;
        this.amenities = "TV, WiFi, AC";

    }

    public Room() {

    }

    // Getters and Setters
    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public String getBedType() { return bedType; }
    public void setBedType(String bedType) { this.bedType = bedType; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isNeedsCleaning() { return needsCleaning; }
    public void setNeedsCleaning(boolean needsCleaning) { this.needsCleaning = needsCleaning; }

    public boolean isNeedsMaintenance() { return needsMaintenance; }
    public void setNeedsMaintenance(boolean needsMaintenance) { this.needsMaintenance = needsMaintenance; }

    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }

    public int getMaxOccupancy() { return maxOccupancy; }
    public void setMaxOccupancy(int maxOccupancy) { this.maxOccupancy = maxOccupancy; }

    public String getViewType() { return viewType; }
    public void setViewType(String viewType) { this.viewType = viewType; }


    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatusColor() {
        return switch (status) {
            case "AVAILABLE" -> "green";
            case "OCCUPIED" -> "red";
            case "RESERVED" -> "orange";
            case "MAINTENANCE" -> "gray";
            case "OUT_OF_ORDER" -> "darkred";
            default -> "black";
        };
    }

    public String getRoomInfo() {
        return String.format(
                """
                Room: %03d
                Type: %s (%s Bed)
                Floor: %d
                Price: $%.2f/night
                Max Guests: %d
                View: %s
                Status: %s
                Amenities: %s
                """,
                roomNumber, roomType, bedType, floor, pricePerNight,
                maxOccupancy, viewType, status, amenities
        );
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " - " + roomType + " ($" + pricePerNight + "/night)";
    }
}