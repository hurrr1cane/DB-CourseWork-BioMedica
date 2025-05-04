import React, { useState, useEffect } from 'react';
import { Bar } from 'react-chartjs-2';
import { useNavigate } from 'react-router-dom';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js';
import Header from '../components/header/Header';
import Footer from '../components/footer/Footer';
import styles from '../styles/analytics_page.module.css';

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
);

export default function AnalyticsPage() {
  const navigate = useNavigate();
  const [year, setYear] = useState(new Date().getFullYear());
  const [monthlyData, setMonthlyData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [availableYears, setAvailableYears] = useState([]);
  
  const userRole = localStorage.getItem("userRole");
  const token = localStorage.getItem("accessToken");
  
  // Redirect if not admin
  useEffect(() => {
    if (!token || userRole !== "ADMINISTRATOR") {
      navigate('/');
    }
  }, [token, userRole, navigate]);
  
  // Generate available years (current year and 5 years back)
  useEffect(() => {
    const currentYear = new Date().getFullYear();
    const years = [];
    for (let i = 0; i < 6; i++) {
      years.push(currentYear - i);
    }
    setAvailableYears(years);
  }, []);
  
  // Fetch data for the selected year
  useEffect(() => {
    const fetchAnalytics = async () => {
      setLoading(true);
      setError(null);
      
      try {
        const response = await fetch(`http://localhost:8080/api/admin/analytics/orders/${year}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        if (!response.ok) {
          throw new Error(`Error fetching analytics: ${response.status}`);
        }
        
        const data = await response.json();
        
        // Fill in missing months with zeros
        const filledData = fillMissingMonths(data);
        setMonthlyData(filledData);
      } catch (err) {
        console.error("Failed to fetch analytics:", err);
        setError(err.message || "Failed to load analytics data");
      } finally {
        setLoading(false);
      }
    };
    
    if (token) {
      fetchAnalytics();
    }
  }, [year, token]);
  
  // Helper to fill in missing months with zero values
  const fillMissingMonths = (data) => {
    const months = Array(12).fill().map((_, i) => i + 1);
    const filledData = [];
    
    months.forEach(month => {
      const existingData = data.find(item => item.month === month);
      if (existingData) {
        filledData.push(existingData);
      } else {
        filledData.push({
          month,
          orderCount: 0,
          totalAmount: 0
        });
      }
    });
    
    return filledData;
  };
  
  // Chart options and data
  const monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];
  
  const orderCountData = {
    labels: monthNames,
    datasets: [
      {
        label: 'Order Count',
        data: monthlyData.map(item => item.orderCount),
        backgroundColor: 'rgba(50, 205, 50, 0.7)',
        borderColor: 'rgba(50, 205, 50, 1)',
        borderWidth: 1
      }
    ]
  };
  
  const revenueData = {
    labels: monthNames,
    datasets: [
      {
        label: 'Revenue ($)',
        data: monthlyData.map(item => parseFloat(item.totalAmount || 0)),
        backgroundColor: 'rgba(54, 162, 235, 0.7)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1
      }
    ]
  };
  
  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        labels: {
          color: '#ddd'
        }
      },
      tooltip: {
        callbacks: {
          label: function(context) {
            if (context.dataset.label === 'Revenue ($)') {
              return `Revenue: $${context.raw.toFixed(2)}`;
            }
            return `Orders: ${context.raw}`;
          }
        }
      }
    },
    scales: {
      x: {
        grid: {
          color: 'rgba(255, 255, 255, 0.1)'
        },
        ticks: {
          color: '#ddd'
        }
      },
      y: {
        beginAtZero: true,
        grid: {
          color: 'rgba(255, 255, 255, 0.1)'
        },
        ticks: {
          color: '#ddd'
        }
      }
    }
  };
  
  const handleYearChange = (e) => {
    setYear(parseInt(e.target.value));
  };
  
  const handlePrevYear = () => {
    setYear(prevYear => Math.max(2000, prevYear - 1));
  };
  
  const handleNextYear = () => {
    setYear(prevYear => Math.min(2100, prevYear + 1));
  };
  
  const calculateYearlyTotals = () => {
    const totalOrders = monthlyData.reduce((sum, month) => sum + month.orderCount, 0);
    const totalRevenue = monthlyData.reduce((sum, month) => sum + parseFloat(month.totalAmount || 0), 0);
    return { totalOrders, totalRevenue };
  };
  
  const { totalOrders, totalRevenue } = calculateYearlyTotals();
  
  return (
    <div className={styles.analytics_container}>
      <Header />
      <main className={styles.content}>
        <h1 className={styles.title}>Order Analytics Dashboard</h1>
        
        <div className={styles.year_selector}>
          <button 
            className={styles.year_nav_button}
            onClick={handlePrevYear}
            disabled={year <= 2000}
          >
            ◀
          </button>
          <select
            value={year}
            onChange={handleYearChange}
            className={styles.year_dropdown}
          >
            {availableYears.map(y => (
              <option key={y} value={y}>{y}</option>
            ))}
            <option value="custom">Custom Year...</option>
          </select>
          <button
            className={styles.year_nav_button}
            onClick={handleNextYear}
            disabled={year >= 2100}
          >
            ▶
          </button>
          
          {year === 'custom' && (
            <input
              type="number"
              min="2000"
              max="2100"
              className={styles.custom_year_input}
              placeholder="Enter year"
              onChange={(e) => setYear(parseInt(e.target.value))}
            />
          )}
        </div>
        
        {loading ? (
          <div className={styles.loading_container}>
            <div className={styles.loading_spinner}></div>
            <p>Loading analytics data...</p>
          </div>
        ) : error ? (
          <div className={styles.error_container}>
            <p className={styles.error_message}>{error}</p>
            <button 
              className={styles.retry_button}
              onClick={() => window.location.reload()}
            >
              Retry
            </button>
          </div>
        ) : (
          <>
            <div className={styles.summary_cards}>
              <div className={styles.summary_card}>
                <h3>Total Orders ({year})</h3>
                <div className={styles.summary_value}>{totalOrders}</div>
              </div>
              <div className={styles.summary_card}>
                <h3>Total Revenue ({year})</h3>
                <div className={styles.summary_value}>${totalRevenue.toFixed(2)}</div>
              </div>
            </div>
            
            <div className={styles.charts_container}>
              <div className={styles.chart_wrapper}>
                <h2 className={styles.chart_title}>Monthly Order Count ({year})</h2>
                <div className={styles.chart}>
                  <Bar data={orderCountData} options={chartOptions} />
                </div>
              </div>
              
              <div className={styles.chart_wrapper}>
                <h2 className={styles.chart_title}>Monthly Revenue ({year})</h2>
                <div className={styles.chart}>
                  <Bar data={revenueData} options={chartOptions} />
                </div>
              </div>
            </div>
            
            <div className={styles.data_table_container}>
              <h2 className={styles.table_title}>Monthly Data ({year})</h2>
              <div className={styles.table_wrapper}>
                <table className={styles.data_table}>
                  <thead>
                    <tr>
                      <th>Month</th>
                      <th>Order Count</th>
                      <th>Revenue</th>
                    </tr>
                  </thead>
                  <tbody>
                    {monthlyData.map(item => (
                      <tr key={item.month}>
                        <td>{monthNames[item.month - 1]}</td>
                        <td>{item.orderCount}</td>
                        <td>${parseFloat(item.totalAmount || 0).toFixed(2)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </>
        )}
      </main>
      <Footer />
    </div>
  );
}