import { useState } from 'react'
import { fetchMonthlyForecast, type MonthlyForecastResponse } from '../api/analyticsApi'

type LoadState = 'idle' | 'loading' | 'ready' | 'error'

function ForecastPanel() {
  const [periods, setPeriods] = useState(6)
  const [forecast, setForecast] = useState<MonthlyForecastResponse | null>(null)
  const [loadState, setLoadState] = useState<LoadState>('idle')

  async function handleRunForecast() {
    try {
      setLoadState('loading')
      const data = await fetchMonthlyForecast(periods)
      setForecast(data)
      setLoadState('ready')
    } catch (error) {
      console.error(error)
      setLoadState('error')
    }
  }

  return (
    <section className="panel">
      <div className="panel-header">
        <h2>Monthly Request Forecast</h2>
        <p>Baseline forecast using recent average month-to-month change.</p>
      </div>

      <div className="forecast-controls">
        <label>
          Recent periods
          <input
            type="number"
            min="3"
            max="12"
            value={periods}
            onChange={(event) => setPeriods(Number(event.target.value))}
          />
        </label>

        <button type="button" onClick={handleRunForecast}>
          Run Forecast
        </button>
      </div>

      {loadState === 'loading' && <p>Running forecast...</p>}
      {loadState === 'error' && <p>Forecast data is unavailable.</p>}

      {loadState === 'ready' && forecast && (
        <>
          <div className="forecast-summary">
            <article>
              <span>Latest Period</span>
              <strong>
                {forecast.latestYear}-{forecast.latestMonth.toString().padStart(2, '0')}
              </strong>
            </article>
            <article>
              <span>Latest Count</span>
              <strong>{forecast.latestRequestCount.toLocaleString()}</strong>
            </article>
            <article>
              <span>Average Change</span>
              <strong>{forecast.averageMonthlyChange.toFixed(2)}</strong>
            </article>
            <article>
              <span>Forecast Count</span>
              <strong>{forecast.forecastRequestCount.toLocaleString()}</strong>
            </article>
          </div>

          <p className="forecast-method">
            {forecast.method} · {forecast.periodsUsed} periods used
          </p>
        </>
      )}
    </section>
  )
}

export default ForecastPanel
