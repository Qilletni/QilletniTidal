import "tidal:tidal.ql"

provider "tidal"

collection demo = "Demo Playlist" collection by "rubbaboy"

print(demo.getTrackCount())
print(demo.getId())

play demo
