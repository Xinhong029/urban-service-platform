// This component is a reusable panel for 
// displaying a list of items as horizontal bars.


// Define the possible states for loading data in the component.
type LoadState = 'loading' | 'ready' | 'error'

// The BarListPanel component is designed to be flexible 
// and can be used to display any type of data
type BarListPanelProps<T> = {
  title: string
  description: string
  items: T[]
  loadState: LoadState
  loadingMessage: string
  errorMessage: string
  getKey: (item: T) => string
  getLabel: (item: T) => string
  getValue: (item: T) => number
  barClassName?: string
}

function BarListPanel<T>({
  title,
  description,
  items,
  loadState,
  loadingMessage,
  errorMessage,
  getKey,
  getLabel,
  getValue,
  barClassName = '',
}: BarListPanelProps<T>) {
  const maxValue = Math.max(...items.map((item) => getValue(item)), 1)

  return (
    <section className="panel">
      <div className="panel-header">
        <h2>{title}</h2>
        <p>{description}</p>
      </div>

      {loadState === 'loading' && <p>{loadingMessage}</p>}
      {loadState === 'error' && <p>{errorMessage}</p>}

      {loadState === 'ready' && (
        <div className="bar-list">
          {items.map((item) => {
            const value = getValue(item)

            return (
              <div className="bar-row" key={getKey(item)}>
                <div className="bar-row__label">
                  <span>{getLabel(item)}</span>
                  <strong>{value.toLocaleString()}</strong>
                </div>
                <div className="bar-track" aria-hidden="true">
                  <div
                    className={`bar-fill ${barClassName}`.trim()}
                    style={{
                      width: `${(value / maxValue) * 100}%`,
                    }}
                  ></div>
                </div>
              </div>
            )
          })}
        </div>
      )}
    </section>
  )
}

export default BarListPanel
