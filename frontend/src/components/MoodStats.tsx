import React, { useState, useEffect } from 'react';
import { moodService } from '../services/moodService';
import type { MoodStatsResponse } from '../services/moodService';
import '../styles/MoodStats.css';

interface MoodStatsProps {
  onError?: (error: string) => void;
}

interface ChartData {
  labels: string[];
  values: number[];
}

export const MoodStats: React.FC<MoodStatsProps> = ({ onError }) => {
  const [trends, setTrends] = useState<MoodStatsResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [period, setPeriod] = useState<string>('30days');
  const [chartType, setChartType] = useState<'bar' | 'pie'>('bar');
  const [emotionChartData, setEmotionChartData] = useState<ChartData | null>(null);

  useEffect(() => {
    fetchMoodTrends();
  }, [period]);

  const fetchMoodTrends = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const data = await moodService.getMoodStats(period === '7days' ? 7 : period === '30days' ? 30 : period === '90days' ? 90 : 365);
      setTrends(data);

      if (data.moodDistribution && Object.keys(data.moodDistribution).length > 0) {
        const labels = Object.keys(data.moodDistribution).map(k => `Mood ${k}`);
        const values = Object.values(data.moodDistribution).map((v: number) => v);
        setEmotionChartData({ labels, values });
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to fetch mood trends';
      setError(errorMessage);
      onError?.(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const getTrendIcon = (direction: string): string => {
    switch (direction) {
      case 'IMPROVING':
        return '📈';
      case 'DECLINING':
        return '📉';
      case 'STABLE':
        return '➡️';
      default:
        return '❓';
    }
  };

  const getTrendColor = (direction: string): string => {
    switch (direction) {
      case 'IMPROVING':
        return '#22c55e';
      case 'DECLINING':
        return '#ef4444';
      case 'STABLE':
        return '#3b82f6';
      default:
        return '#6b7280';
    }
  };

  const renderBarChart = (data: ChartData) => {
    const maxValue = Math.max(...data.values);
    const barHeight = 200;

    return (
      <div className="bar-chart">
        <div className="chart-bars">
          {data.labels.map((label, index) => (
            <div key={label} className="bar-container">
              <div
                className="bar"
                style={{
                  height: `${(data.values[index] / maxValue) * barHeight}px`,
                  backgroundColor: `hsl(${(index * 360) / data.labels.length}, 70%, 50%)`,
                }}
              />
              <div className="bar-label">{label}</div>
              <div className="bar-value">{data.values[index]}</div>
            </div>
          ))}
        </div>
      </div>
    );
  };

  const renderPieChart = (data: ChartData) => {
    const total = data.values.reduce((a, b) => a + b, 0);
    let currentAngle = 0;
    const slices = data.labels.map((label, index) => {
      const sliceAngle = (data.values[index] / total) * 360;
      const startAngle = currentAngle;
      const endAngle = currentAngle + sliceAngle;
      currentAngle = endAngle;

      const startRad = (startAngle * Math.PI) / 180;
      const endRad = (endAngle * Math.PI) / 180;
      const radius = 80;
      const x1 = 100 + radius * Math.cos(startRad);
      const y1 = 100 + radius * Math.sin(startRad);
      const x2 = 100 + radius * Math.cos(endRad);
      const y2 = 100 + radius * Math.sin(endRad);

      const largeArc = sliceAngle > 180 ? 1 : 0;

      const pathData = [
        `M 100 100`,
        `L ${x1} ${y1}`,
        `A ${radius} ${radius} 0 ${largeArc} 1 ${x2} ${y2}`,
        'Z',
      ].join(' ');

      return {
        path: pathData,
        color: `hsl(${(index * 360) / data.labels.length}, 70%, 50%)`,
        label,
        percentage: ((data.values[index] / total) * 100).toFixed(1),
      };
    });

    return (
      <div className="pie-chart-container">
        <svg viewBox="0 0 200 200" className="pie-chart">
          {slices.map((slice, index) => (
            <path key={index} d={slice.path} fill={slice.color} stroke="white" strokeWidth="2" />
          ))}
        </svg>
        <div className="pie-legend">
          {slices.map((slice, index) => (
            <div key={index} className="legend-item">
              <div className="legend-color" style={{ backgroundColor: slice.color }} />
              <span className="legend-label">
                {slice.label}: {slice.percentage}%
              </span>
            </div>
          ))}
        </div>
      </div>
    );
  };

  if (isLoading) {
    return <div className="mood-stats-loading">Loading mood statistics...</div>;
  }

  if (!trends) {
    return <div className="mood-stats-error">Unable to load mood statistics</div>;
  }

  return (
    <div className="mood-stats">
      <div className="mood-stats-header">
        <h2>Mood Statistics & Trends</h2>
        <p>Analyze your emotional patterns</p>
      </div>

      {error && <div className="mood-stats-error">{error}</div>}

      {/* Period Selector */}
      <div className="period-selector">
        <label htmlFor="period-select">Time Period:</label>
        <select
          id="period-select"
          value={period}
          onChange={(e) => setPeriod(e.target.value)}
          className="period-select"
        >
          <option value="7days">Last 7 Days</option>
          <option value="30days">Last 30 Days</option>
          <option value="90days">Last 90 Days</option>
          <option value="1year">Last Year</option>
        </select>
      </div>

      {/* Key Metrics */}
      <div className="metrics-grid">
        <div className="metric-card">
          <div className="metric-label">Average Mood</div>
          <div className="metric-value">{(trends.averageMoodRating ?? 0).toFixed(1)}/5</div>
          <div className="metric-bar">
            <div className="metric-bar-fill" style={{
              width: `${((trends.averageMoodRating ?? 0) / 5) * 100}%`,
              backgroundColor: (trends.averageMoodRating ?? 0) >= 3.5 ? '#22c55e' : '#ef4444',
            }}/>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-label">Total Entries</div>
          <div className="metric-value">{trends.totalEntries}</div>
          <div className="metric-description">Over selected period</div>
        </div>

        <div className="metric-card">
          <div className="metric-label">Average Energy</div>
          <div className="metric-value">{(trends.averageEnergyLevel ?? 0).toFixed(1)}/5</div>
          <div className="metric-bar">
            <div className="metric-bar-fill" style={{
              width: `${((trends.averageEnergyLevel ?? 0) / 5) * 100}%`,
              backgroundColor: '#3b82f6',
            }}/>
          </div>
        </div>

        <div className="metric-card">
          <div className="metric-label">Sleep Quality</div>
          <div className="metric-value">{(trends.averageSleepQuality ?? 0).toFixed(1)}/5</div>
          <div className="metric-bar">
            <div className="metric-bar-fill" style={{
              width: `${((trends.averageSleepQuality ?? 0) / 5) * 100}%`,
              backgroundColor: '#8b5cf6',
            }}/>
          </div>
        </div>
      </div>

      {/* Trend Direction */}
      <div className="trend-section">
        <h3>Overall Trend</h3>
        <div className="trend-indicator" style={{ color: getTrendColor(trends.trendDirection) }}>
          <span className="trend-icon">{getTrendIcon(trends.trendDirection)}</span>
          <span className="trend-text">{trends.trendDirection}</span>
        </div>
      </div>

      {/* Emotion Distribution Chart */}
      {emotionChartData && (
        <div className="chart-section">
          <div className="chart-header">
            <h3>Emotion Distribution</h3>
            <div className="chart-type-selector">
              <button
                className={`chart-type-btn ${chartType === 'bar' ? 'active' : ''}`}
                onClick={() => setChartType('bar')}
              >
                Bar Chart
              </button>
              <button
                className={`chart-type-btn ${chartType === 'pie' ? 'active' : ''}`}
                onClick={() => setChartType('pie')}
              >
                Pie Chart
              </button>
            </div>
          </div>
          <div className="chart-container">
            {chartType === 'bar' ? renderBarChart(emotionChartData) : renderPieChart(emotionChartData)}
          </div>
        </div>
      )}

      {/* Recommendations */}
      {trends.recommendations && trends.recommendations.length > 0 && (
        <div className="recommendations-section">
          <h3>💡 Recommendations</h3>
          <ul className="recommendations-list">
            {trends.recommendations.map((rec, index) => (
              <li key={index}>{rec}</li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};
