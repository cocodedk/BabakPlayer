package com.cocode.babakplayer.cast

// Common to both flavors: the foss build never leaves NOT_AVAILABLE, but the enum
// itself carries no Cast SDK dependency, so it stays out of the flavor split.
enum class CastConnectionState {
    NOT_AVAILABLE,
    NOT_CONNECTED,
    CONNECTING,
    CONNECTED,
}
