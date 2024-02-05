package com.microsoft.kiota.serialization;

import java.math.BigDecimal;

/**
 * Represents an untyped node with decimal value.
 */
public class UntypedDecimal extends UntypedNode {
    /**
     * The constructor for the UntypedDecimal
     * @param decimalValue The decimal to create the node with.
     */
    public UntypedDecimal(BigDecimal decimalValue) {
        value = decimalValue;
    }

    private final BigDecimal value;

    /**
     * Gets the value assigned to untyped node.
     * @return The BigDecimal value of the node.
     */
    @Override
    public BigDecimal getValue() {
        return value;
    }
}
