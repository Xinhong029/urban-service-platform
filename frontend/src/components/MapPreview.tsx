// This component provides a simple preview of map points 
// based on latitude and longitude data.

type LoadState = 'loading' | 'ready' | 'error'

type MapPoint = {
  serviceRequestId: string
  serviceName: string
  status: string
  analysisNeighborhood: string
  requestedDatetime: string
  lat: number
  lng: number
}

type MapPreviewProps = {
  points: MapPoint[]
  loadState: LoadState
}

function MapPreview({ points, loadState }: MapPreviewProps) {
  const latValues = points.map((point) => point.lat)
  const lngValues = points.map((point) => point.lng)
  const minLat = Math.min(...latValues)
  const maxLat = Math.max(...latValues)
  const minLng = Math.min(...lngValues)
  const maxLng = Math.max(...lngValues)
  const latRange = maxLat - minLat || 1
  const lngRange = maxLng - minLng || 1

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
          <div className="map-preview" aria-label="Map point preview">
            <svg className="map-preview__plot" viewBox="0 0 100 100" role="img">
              <title>311 request location points</title>
              <rect className="map-preview__background" x="0" y="0" width="100" height="100" />

              {points.map((point) => {
                const x = 4 + ((point.lng - minLng) / lngRange) * 92
                const y = 4 + ((maxLat - point.lat) / latRange) * 92

                return (
                  <circle key={point.serviceRequestId} cx={x} cy={y} r="0.9">
                    <title>
                      {point.serviceName} - {point.analysisNeighborhood}
                    </title>
                  </circle>
                )
              })}
            </svg>
          </div>

          <div className="map-summary">
            <span>{points.length.toLocaleString()} points</span>
            <span>
              Lat {minLat.toFixed(3)} to {maxLat.toFixed(3)}
            </span>
            <span>
              Lng {minLng.toFixed(3)} to {maxLng.toFixed(3)}
            </span>
          </div>
        </>
      )}
    </section>
  )
}

export default MapPreview
