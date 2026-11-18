package com.davidrdz.vitallog.data.entity;

/**
 * Unidad de la dosis de un medicamento. Room la guarda por nombre (TEXT); el texto visible lo
 * resuelve la capa de UI desde strings.xml.
 */
public enum DoseUnit {
    TABLET,
    CAPSULE,
    MILLILITER,
    UNIT
}
