import { useEffect, useState } from 'react'
import {
  fetchAverageResolutionTime,
  fetchBackendHealth,
  fetchMapPoints,
  fetchMonthlyRequestCounts,
  fetchTopNeighborhoods,
  fetchTopServiceTypes,
  type AverageResolutionTimeResponse,
  type HealthResponse,
  type MapPointResponse,
  type MonthlyRequestCountResponse,
  type NeighborhoodCountResponse,
  type ServiceTypeCountResponse,
} from './api/analyticsApi'
import BarListPanel from './components/BarListPanel'
import MapPreview from './components/MapPreview'
import './App.css'

// Define a type for the loading state of API calls
type LoadState = 'loading' | 'ready' | 'error'

// Main application component
function App() {
  // State for backend health status, loading state, average resolution time, and KPI loading state
  const [backendStatus, setBackendStatus] = useState<HealthResponse | null>(null)
  const [loadState, setLoadState] = useState<LoadState>('loading')
  const [averageResolutionTime, setAverageResolutionTime] =
    useState<AverageResolutionTimeResponse | null>(null)
  const [kpiLoadState, setKpiLoadState] = useState<LoadState>('loading')
  const [topServiceTypes, setTopServiceTypes] = useState<ServiceTypeCountResponse[]>([])
  const [serviceTypesLoadState, setServiceTypesLoadState] = useState<LoadState>('loading')
  const [topNeighborhoods, setTopNeighborhoods] = useState<NeighborhoodCountResponse[]>([])
  const [neighborhoodsLoadState, setNeighborhoodsLoadState] = useState<LoadState>('loading')
  const [monthlyRequestCounts, setMonthlyRequestCounts] = useState<MonthlyRequestCountResponse[]>([])
  const [monthlyLoadState, setMonthlyLoadState] = useState<LoadState>('loading')
  const [mapPoints, setMapPoints] = useState<MapPointResponse[]>([])
  const [mapLoadState, setMapLoadState] = useState<LoadState>('loading')

  useEffect(() => {
    async function loadBackendHealth() {
      try {
        const data = await fetchBackendHealth()
        setBackendStatus(data)
        setLoadState('ready')
      } catch (error) {
        console.error(error)
        setLoadState('error')
      }
    }

    async function loadAverageResolutionTime() {
      try {
        const data = await fetchAverageResolutionTime()
        setAverageResolutionTime(data)
        setKpiLoadState('ready')
      } catch (error) {
        console.error(error)
        setKpiLoadState('error')
      }
    }

    async function loadTopServiceTypes() {
      try {
        const data = await fetchTopServiceTypes()
        setTopServiceTypes(data)
        setServiceTypesLoadState('ready')
      } catch (error) {
        console.error(error)
        setServiceTypesLoadState('error')
      }
    }

    async function loadTopNeighborhoods() {
      try {
        const data = await fetchTopNeighborhoods()
        setTopNeighborhoods(data)
        setNeighborhoodsLoadState('ready')
      } catch (error) {
        console.error(error)
        setNeighborhoodsLoadState('error')
      }
    }

    async function loadMonthlyRequestCounts() {
      try {
        const data = await fetchMonthlyRequestCounts()
        setMonthlyRequestCounts(data)
        setMonthlyLoadState('ready')
      } catch (error) {
        console.error(error)
        setMonthlyLoadState('error')
      }
    }

    async function loadMapPoints() {
      try {
        const data = await fetchMapPoints()
        setMapPoints(data)
        setMapLoadState('ready')
      } catch (error) {
        console.error(error)
        setMapLoadState('error')
      }
    }

    loadBackendHealth() // Call the health check function when the component mounts
    loadAverageResolutionTime()
    loadTopServiceTypes()
    loadTopNeighborhoods()
    loadMonthlyRequestCounts()
    loadMapPoints()
  }, []) 

  const statusText =
    loadState === 'ready' ? backendStatus?.status : loadState === 'loading' ? 'checking' : 'offline'
  const averageResolutionText =
    kpiLoadState === 'ready'
      ? `${averageResolutionTime?.averageResolutionHours.toFixed(2)} hours`
      : kpiLoadState === 'loading'
        ? 'loading'
        : 'unavailable'

  return (
    <main className="dashboard">
      <section className="hero">
        <div>
          <p className="eyebrow">Urban operations analytics</p>
          <h1>Urban Service Platform</h1>
          <p className="lede">
            A dashboard for exploring 311 service request volume, neighborhood hotspots,
            response time, and map-ready operational data.
          </p>
        </div>
      </section>

      <section className="status-grid" aria-label="System status">
        <article className="status-card">
          <span className={`status-dot status-dot--${loadState}`}></span>
          <div>
            <h2>Backend Status</h2>
            <p>{statusText}</p>
          </div>
        </article>

        <article className="status-card">
          <span className="status-dot status-dot--ready"></span>
          <div>
            <h2>API Base</h2>
            <p>/api</p>
          </div>
        </article>

        <article className="status-card metric-card">
          <span className={`status-dot status-dot--${kpiLoadState}`}></span>
          <div>
            <h2>Average Resolution Time</h2>
            <p>{averageResolutionText}</p>
          </div>
        </article>
      </section>

      <BarListPanel
        title="Top Service Types"
        description="Most common 311 request categories in the cleaned dataset."
        items={topServiceTypes}
        loadState={serviceTypesLoadState}
        loadingMessage="Loading service type data..."
        errorMessage="Service type data is unavailable."
        getKey={(serviceType) => serviceType.serviceName}
        getLabel={(serviceType) => serviceType.serviceName}
        getValue={(serviceType) => serviceType.requestCount}
      />

      <BarListPanel
        title="Monthly Request Trend"
        description="Request volume grouped by year and month."
        items={monthlyRequestCounts}
        loadState={monthlyLoadState}
        loadingMessage="Loading monthly request data..."
        errorMessage="Monthly request data is unavailable."
        getKey={(monthlyCount) => `${monthlyCount.year}-${monthlyCount.month}`}
        getLabel={(monthlyCount) =>
          `${monthlyCount.year}-${monthlyCount.month.toString().padStart(2, '0')}`
        }
        getValue={(monthlyCount) => monthlyCount.requestCount}
        barClassName="bar-fill--monthly"
      />

      <BarListPanel
        title="Top Neighborhoods"
        description="Neighborhoods with the highest 311 request volume."
        items={topNeighborhoods}
        loadState={neighborhoodsLoadState}
        loadingMessage="Loading neighborhood data..."
        errorMessage="Neighborhood data is unavailable."
        getKey={(neighborhood) => neighborhood.neighborhood}
        getLabel={(neighborhood) => neighborhood.neighborhood}
        getValue={(neighborhood) => neighborhood.requestCount}
        barClassName="bar-fill--neighborhood"
      />

      <MapPreview points={mapPoints} loadState={mapLoadState} />

      <section className="panel">
        <div className="panel-header">
          <h2>Dashboard API Plan</h2>
          <p>These backend endpoints are ready to connect to charts and maps.</p>
        </div>

        <div className="endpoint-list">
          <code>GET /api/analytics/top-service-types</code>
          <code>GET /api/analytics/top-neighborhoods</code>
          <code>GET /api/analytics/monthly-request-counts</code>
          <code>GET /api/analytics/average-resolution-time</code>
          <code>GET /api/analytics/map-points?limit=500</code>
        </div>
      </section>
    </main>
  )
}

export default App
