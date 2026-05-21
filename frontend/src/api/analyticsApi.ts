export type HealthResponse = {
  status: string
}

export type AverageResolutionTimeResponse = {
  averageResolutionHours: number
}

export type ServiceTypeCountResponse = {
  serviceName: string
  requestCount: number
}

export type NeighborhoodCountResponse = {
  neighborhood: string
  requestCount: number
}

export type MonthlyRequestCountResponse = {
  year: number
  month: number
  requestCount: number
}

export type MapPointResponse = {
  serviceRequestId: string
  serviceName: string
  status: string
  analysisNeighborhood: string
  requestedDatetime: string
  lat: number
  lng: number
}

async function fetchJson<T>(url: string, errorMessage: string): Promise<T> {
  const response = await fetch(url)

  if (!response.ok) {
    throw new Error(`${errorMessage} with status ${response.status}`)
  }

  return (await response.json()) as T
}

export function fetchBackendHealth() {
  return fetchJson<HealthResponse>('/api/health', 'Health check failed')
}

export function fetchAverageResolutionTime() {
  return fetchJson<AverageResolutionTimeResponse>(
    '/api/analytics/average-resolution-time',
    'Average resolution request failed',
  )
}

export function fetchTopServiceTypes() {
  return fetchJson<ServiceTypeCountResponse[]>(
    '/api/analytics/top-service-types',
    'Top service types request failed',
  )
}

export function fetchTopNeighborhoods() {
  return fetchJson<NeighborhoodCountResponse[]>(
    '/api/analytics/top-neighborhoods',
    'Top neighborhoods request failed',
  )
}

export function fetchMonthlyRequestCounts() {
  return fetchJson<MonthlyRequestCountResponse[]>(
    '/api/analytics/monthly-request-counts',
    'Monthly request counts request failed',
  )
}

export function fetchMapPoints(limit = 500) {
  return fetchJson<MapPointResponse[]>(
    `/api/analytics/map-points?limit=${limit}`,
    'Map points request failed',
  )
}
