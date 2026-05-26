import { useState } from 'react'
import type { MapPointResponse } from '../api/analyticsApi'

// This component provides a simple preview of map points
// based on latitude and longitude data.

type LoadState = 'loading' | 'ready' | 'error'

type MapPreviewProps = {
  points: MapPointResponse[]
  loadState: LoadState
}

function getStatusClassName(status: string) {
  const normalizedStatus = status.toLowerCase()

  if (normalizedStatus.includes('closed')) {
    return 'map-point--closed'
  }

  if (normalizedStatus.includes('open')) {
    return 'map-point--open'
  }

  return 'map-point--other'
}

function getTopHotspots(points: MapPointResponse[]) {
  const hotspotCounts = new Map<string, number>()

  points.forEach((point) => {
    const neighborhood = point.analysisNeighborhood || 'Unknown'
    hotspotCounts.set(neighborhood, (hotspotCounts.get(neighborhood) ?? 0) + 1)
  })

  return Array.from(hotspotCounts.entries())
    .map(([neighborhood, requestCount]) => ({ neighborhood, requestCount }))
    .sort((firstHotspot, secondHotspot) => secondHotspot.requestCount - firstHotspot.requestCount)
    .slice(0, 5)
}

function MapPreview({ points, loadState }: MapPreviewProps) {
  const [selectedPoint, setSelectedPoint] = useState<MapPointResponse | null>(null)
  const latValues = points.map((point) => point.lat)
  const lngValues = points.map((point) => point.lng)
  const minLat = Math.min(...latValues)
  const maxLat = Math.max(...latValues)
  const minLng = Math.min(...lngValues)
  const maxLng = Math.max(...lngValues)
  const latRange = maxLat - minLat || 1
  const lngRange = maxLng - minLng || 1
  const closedCount = points.filter((point) => getStatusClassName(point.status) === 'map-point--closed')
    .length
  const openCount = points.filter((point) => getStatusClassName(point.status) === 'map-point--open').length
  const otherCount = points.length - closedCount - openCount
  const displayPoint = selectedPoint ?? points[0]
  const hotspots = getTopHotspots(points)
  const maxHotspotCount = hotspots[0]?.requestCount ?? 1

  // The component renders a simple SVG plot of the points,
  // along with a summary of the number of points and their lat/lng ranges.
  return (
    <section className="panel">
      <div className="panel-header">
        <h2>Map Points Preview</h2>
        <p>Recent 311 requests plotted from latitude and longitude.</p>
      </div>

      {loadState === 'loading' && <p>Loading map point data...</p>}
      {loadState === 'error' && <p>Map point data is unavailable.</p>}

      {loadState === 'ready' && (
        <>
          <div className="map-layout">
            <div className="map-preview" aria-label="Map point preview">
              <svg className="map-preview__plot" viewBox="0 0 100 100" role="img">
                <title>311 request location points</title>
                <rect className="map-preview__background" x="0" y="0" width="100" height="100" />
                <path className="map-preview__grid" d="M20 0V100 M40 0V100 M60 0V100 M80 0V100 M0 20H100 M0 40H100 M0 60H100 M0 80H100" />

                {points.map((point) => {
                  const x = 4 + ((point.lng - minLng) / lngRange) * 92
                  const y = 4 + ((maxLat - point.lat) / latRange) * 92
                  const isSelected = selectedPoint?.serviceRequestId === point.serviceRequestId

                  return (
                    <circle
                      className={`map-point ${getStatusClassName(point.status)} ${
                        isSelected ? 'map-point--selected' : ''
                      }`}
                      key={point.serviceRequestId}
                      cx={x}
                      cy={y}
                      r={isSelected ? '1.8' : '1'}
                      onClick={() => setSelectedPoint(point)}
                    >
                      <title>
                        {point.serviceName} - {point.analysisNeighborhood}
                      </title>
                    </circle>
                  )
                })}
              </svg>
            </div>

            <aside className="map-details" aria-label="Selected map point details">
              <div>
                <h3>{displayPoint.serviceName}</h3>
                <p>{displayPoint.analysisNeighborhood}</p>
              </div>

              <dl>
                <div>
                  <dt>Status</dt>
                  <dd>{displayPoint.status}</dd>
                </div>
                <div>
                  <dt>Requested</dt>
                  <dd>{new Date(displayPoint.requestedDatetime).toLocaleDateString()}</dd>
                </div>
                <div>
                  <dt>Latitude</dt>
                  <dd>{displayPoint.lat.toFixed(4)}</dd>
                </div>
                <div>
                  <dt>Longitude</dt>
                  <dd>{displayPoint.lng.toFixed(4)}</dd>
                </div>
              </dl>
            </aside>
          </div>

          <div className="map-hotspots">
            <div className="map-hotspots__header">
              <h3>Map Hotspots</h3>
              <p>Top neighborhoods in the current point sample.</p>
            </div>

            <div className="map-hotspots__list">
              {hotspots.map((hotspot) => (
                <article className="map-hotspot-row" key={hotspot.neighborhood}>
                  <div className="map-hotspot-row__label">
                    <span>{hotspot.neighborhood}</span>
                    <strong>{hotspot.requestCount.toLocaleString()}</strong>
                  </div>
                  <div className="bar-track" aria-hidden="true">
                    <div
                      className="bar-fill bar-fill--hotspot"
                      style={{
                        width: `${(hotspot.requestCount / maxHotspotCount) * 100}%`,
                      }}
                    ></div>
                  </div>
                </article>
              ))}
            </div>
          </div>

          <div className="map-summary">
            <span>{points.length.toLocaleString()} points</span>
            <span>{closedCount.toLocaleString()} closed</span>
            <span>{openCount.toLocaleString()} open</span>
            <span>{otherCount.toLocaleString()} other</span>
            <span>
              Lat {minLat.toFixed(3)} to {maxLat.toFixed(3)}
            </span>
            <span>
              Lng {minLng.toFixed(3)} to {maxLng.toFixed(3)}
            </span>
          </div>

          <div className="map-legend" aria-label="Map legend">
            <span><i className="map-legend__dot map-legend__dot--closed"></i>Closed</span>
            <span><i className="map-legend__dot map-legend__dot--open"></i>Open</span>
            <span><i className="map-legend__dot map-legend__dot--other"></i>Other</span>
          </div>
        </>
      )}
    </section>
  )
}

export default MapPreview
