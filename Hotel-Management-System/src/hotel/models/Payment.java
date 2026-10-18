package hotel.models;

import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private String paymentType;
    private int bookingId;
    private double amount;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String status;
    private String transactionId;
    private String notes;
    private LocalDateTime createdAt;

    // Constructors
    public Payment() {
        this.paymentDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.status = "COMPLETED";
    }

    public Payment(int bookingId, double amount, String paymentMethod) {
        this();
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }


    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentType() {
        return paymentType;
    }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }


    public boolean isSuccessful() {
        return "COMPLETED".equals(status);
    }

    public boolean isRefundable() {
        return isSuccessful() && paymentDate.isAfter(LocalDateTime.now().minusDays(30));
    }

    @Override
    public String toString() {
        return "Payment $" + amount + " - " + paymentMethod + " - " + status;
    }
}