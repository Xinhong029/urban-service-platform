import type { NeighborhoodRiskResponse } from '../api/analyticsApi'

type LoadState = 'loading' | 'ready' | 'error'

type RiskScorePanelProps = {
  risks: NeighborhoodRiskResponse[]
  loadState: LoadState
}

function RiskScorePanel({ risks, loadState }: RiskScorePanelProps) {
  return (
    <section className="panel">
      <div className="panel-header">
        <h2>Neighborhood Risk Scores</h2>
        <p>Simple operational risk based on request volume and resolution time.</p>
      </div>

      {loadState === 'loading' && <p>Loading neighborhood risk data...</p>}
      {loadState === 'error' && <p>Neighborhood risk data is unavailable.</p>}

      {loadState === 'ready' && (
        <div className="risk-list">
          {risks.map((risk) => (
            <article className="risk-row" key={risk.neighborhood}>
              <div className="risk-row__main">
                <div>
                  <h3>{risk.neighborhood}</h3>
                  <p>
                    {risk.requestCount.toLocaleString()} requests ·{' '}
                    {risk.averageResolutionHours.toFixed(2)} avg resolution hours
                  </p>
                </div>
                <strong>{risk.riskScore.toFixed(2)}</strong>
              </div>

              <div className="bar-track" aria-hidden="true">
                <div
                  className="bar-fill bar-fill--risk"
                  style={{
                    width: `${risk.riskScore}%`,
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

export default RiskScorePanel
