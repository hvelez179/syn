//
// SummaryTextId.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.enumerations

/**
 * This enum lists the messages displayed on the dashboard.
 * OVERUSE - message is displayed when more than 8 inhalations occur in the same day.
 * NO_INHALERS - message is displayed before any inhaler is scanned.
 * EMPTY_INHALER - message is displayed when an active inhaler is empty.
 * ENVIRONMENT_MESSAGE - an environment message.
 *
 *
 * Messages in the enum are added in descending order of priority.
 */

enum class SummaryTextId {
    OVERUSE,
    NO_INHALERS,
    EMPTY_INHALER,
    ENVIRONMENT_MESSAGE,
    NEUTRAL_MESSAGE
}
