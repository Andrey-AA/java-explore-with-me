package ru.practicum.model;

public enum RequestStatus {

    PENDING,
    /**
     * В ОЖИДАНИИ
     */

    CONFIRMED,
    /**
     * ПОДТВЕРЖДЕННЫЙ
     */

    REJECTED,
    /**
     * ОТКЛОНЕННЫЙ
     */

    CANCELED /** ОТМЕНЕНО */
}
