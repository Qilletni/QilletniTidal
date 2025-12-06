import "tidal:tidal.ql"
import "std:provider.ql"

provider "tidal"

setFetchResolveStrategy("fuzzy")

song jealous = "Jealous" by "Chromeo"

// Resolves to "Jealous (I Ain't With It)" by "Chromeo", which is a fuzzy match
// "Jealous" by "Chromeo" is now a registered alias, and does not need fuzzy searching for future lookups
printf("Song: %s  %s", [jealous.getTitle(), jealous.getId()])
