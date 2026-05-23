import { useState } from 'react'
import {
  fetchNeighborhoodRiskSimulation,
  type NeighborhoodRiskSimulationResponse,
} from '../api/analyticsApi'

type LoadState = 'idle' | 'loading' | 'ready' | 'error'

function SimulationPanel() {
  const [neighborhood, setNeighborhood] = useState('Bayview Hunters Point')
  const [growthPercent, setGrowthPercent] = useState(50)
  const [simulationResults, setSimulationResults] = useState<NeighborhoodRiskSimulationResponse[]>(
    [],
  )
  const [loadState, setLoadState] = useState<LoadState>('idle')

  async function handleRunSimulation() {
    try {
      setLoadState('loading')
      const data = await fetchNeighborhoodRiskSimulation(neighborhood, growthPercent)
      setSimulationResults(data)
      setLoadState('ready')
    } catch (error) {
      console.error(error)
      setLoadState('error')
    }
  }

  const targetResult = simulationResults.find((result) => result.neighborhood === neighborhood)

  return (
    <section className="panel">
      <div className="panel-header">
        <h2>Targeted Risk Simulation</h2>
        <p>Simulate request growth for one neighborhood and compare risk scores.</p>
      </div>

      <div className="simulation-controls">
        <label>
          Neighborhood
          <input
            value={neighborhood}
            onChange={(event) => setNeighborhood(event.target.value)}
          />
        </label>

        <label>
          Growth percent
          <input
            type="number"
            value={growthPercent}
            onChange={(event) => setGrowthPercent(Number(event.target.value))}
          />
        </label>

        <button type="button" onClick={handleRunSimulation}>
          Run Simulation
        </button>
      </div>

      {loadState === 'loading' && <p>Running simulation...</p>}
      {loadState === 'error' && <p>Simulation data is unavailable.</p>}

      {loadState === 'ready' && targetResult && (
        <div className="simulation-summary">
          <article>
            <span>Current Requests</span>
            <strong>{targetResult.currentRequestCount.toLocaleString()}</strong>
          </article>
          <article>
            <span>Simulated Requests</span>
            <strong>{targetResult.simulatedRequestCount.toLocaleString()}</strong>
          </article>
          <article>
            <span>Current Risk</span>
            <strong>{targetResult.currentRiskScore.toFixed(2)}</strong>
          </article>
          <article>
            <span>Simulated Risk</span>
            <strong>{targetResult.simulatedRiskScore.toFixed(2)}</strong>
          </article>
        </div>
      )}

      {loadState === 'ready' && (
        <div className="simulation-results">
          {simulationResults.map((result) => (
            <article className="risk-row" key={result.neighborhood}>
              <div className="risk-row__main">
                <div>
                  <h3>{result.neighborhood}</h3>
                  <p>
                    {result.currentRequestCount.toLocaleString()} to{' '}
                    {result.simulatedRequestCount.toLocaleString()} requests
                  </p>
                </div>
                <strong>{result.simulatedRiskScore.toFixed(2)}</strong>
              </div>

              <div className="bar-track" aria-hidden="true">
                <div
                  className="bar-fill bar-fill--simulation"
                  style={{
                    width: `${result.simulatedRiskScore}%`,
                  }}
                ></div>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}

export default SimulationPanel
