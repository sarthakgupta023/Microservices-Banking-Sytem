import { useEffect, useState } from 'react';
import Navbar from '../components/Navbar';
import { accountAPI, transactionAPI } from '../services/api';

const fmt = n =>
  '₹' + parseFloat(n || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 });

const fmtDate = (d) => {
  if (!d) return '—';
  const date = new Date(d);
  return date.toLocaleString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
};

export default function History() {
  const [accounts, setAccounts]   = useState([]);
  const [selected, setSelected]   = useState('');
  const [txns, setTxns]           = useState([]);
  const [loading, setLoading]     = useState(false);
  const [filter, setFilter]       = useState('ALL');

  useEffect(() => {
    accountAPI.getByUser(1).then(r => {
      setAccounts(r.data);
      if (r.data.length > 0) {
        setSelected(r.data[0].accountNumber);
      }
    }).catch(() => {});
  }, []);

  useEffect(() => {
    if (!selected) return;
    setLoading(true);
    transactionAPI.getHistory(selected)
      .then(r => setTxns(r.data || []))
      .catch(() => setTxns([]))
      .finally(() => setLoading(false));
  }, [selected]);

  const filtered = txns.filter(t =>
    filter === 'ALL' ? true : t.type === filter
  );

  const totalIn  = txns
    .filter(t => t.toAccountNumber === selected)
    .reduce((s, t) => s + parseFloat(t.amount || 0), 0);
  const totalOut = txns
    .filter(t => t.fromAccountNumber === selected)
    .reduce((s, t) => s + parseFloat(t.amount || 0), 0);

  const isCredit = (t) => t.toAccountNumber === selected;

  return (
    <div style={{ minHeight: '100vh', background: '#0f1a08' }}>
      <Navbar />
      <div style={{ maxWidth: 800, margin: '0 auto', padding: '40px 24px' }}>
        <div className="fade-in">

          {/* Header */}
          <div style={{ marginBottom: 28 }}>
            <h2 style={{ fontSize: 22, fontWeight: 700, color: '#fff' }}>
              Transaction History
            </h2>
            <p style={{ color: 'rgba(255,255,255,0.35)', fontSize: 13, marginTop: 4 }}>
              View all your past transactions
            </p>
          </div>

          {/* Account selector */}
          <div className="glass-card" style={{ padding: 20, marginBottom: 20 }}>
            <div style={{
              display: 'flex', gap: 12,
              alignItems: 'center', flexWrap: 'wrap',
            }}>
              <label className="field-label" style={{ margin: 0, whiteSpace: 'nowrap' }}>
                Account:
              </label>
              <select
                value={selected}
                onChange={e => setSelected(e.target.value)}
                className="olive-input"
                style={{ flex: 1, minWidth: 200 }}>
                {accounts.map(a => (
                  <option key={a.id} value={a.accountNumber}>
                    {a.accountType} — {a.accountNumber}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Stats */}
          <div style={{
            display: 'grid', gridTemplateColumns: 'repeat(3,1fr)',
            gap: 12, marginBottom: 20,
          }}>
            {[
              { label: 'Total Transactions', val: txns.length,     color: '#fff' },
              { label: 'Total Credited',     val: fmt(totalIn),    color: '#7ec87e' },
              { label: 'Total Debited',      val: fmt(totalOut),   color: '#e07c7c' },
            ].map(s => (
              <div key={s.label} className="glass-card" style={{ padding: '16px 20px' }}>
                <p style={{
                  color: 'rgba(255,255,255,0.35)',
                  fontSize: 11, letterSpacing: 1,
                  textTransform: 'uppercase', marginBottom: 6,
                }}>{s.label}</p>
                <p style={{ color: s.color, fontSize: 20, fontWeight: 700 }}>
                  {s.val}
                </p>
              </div>
            ))}
          </div>

          {/* Filter tabs */}
          <div style={{
            display: 'flex', gap: 8, marginBottom: 16,
          }}>
            {['ALL', 'CREDIT', 'DEBIT'].map(f => (
              <button
                key={f}
                onClick={() => setFilter(f)}
                style={{
                  padding: '8px 20px', borderRadius: 20,
                  border: filter === f
                    ? '1px solid #c9a84c'
                    : '1px solid rgba(255,255,255,0.1)',
                  background: filter === f
                    ? 'rgba(201,168,76,0.15)' : 'rgba(255,255,255,0.03)',
                  color: filter === f
                    ? '#f0c96a' : 'rgba(255,255,255,0.4)',
                  fontSize: 12, fontWeight: 600,
                  cursor: 'pointer', transition: 'all 0.15s',
                  letterSpacing: 0.5,
                }}>{f}</button>
            ))}
          </div>

          {/* Transaction list */}
          <div className="glass-card" style={{ overflow: 'hidden' }}>

            {loading && (
              <div style={{ textAlign: 'center', padding: 48 }}>
                <div style={{
                  width: 32, height: 32, borderRadius: '50%',
                  border: '2px solid rgba(201,168,76,0.2)',
                  borderTop: '2px solid #c9a84c',
                  animation: 'spin 0.8s linear infinite',
                  margin: '0 auto',
                }}/>
                <style>{`@keyframes spin{to{transform:rotate(360deg)}}`}</style>
                <p style={{ color: 'rgba(255,255,255,0.3)', fontSize: 13, marginTop: 12 }}>
                  Loading transactions...
                </p>
              </div>
            )}

            {!loading && filtered.length === 0 && (
              <div style={{ textAlign: 'center', padding: 60 }}>
                <p style={{ fontSize: 40, marginBottom: 12 }}>📋</p>
                <p style={{ color: 'rgba(255,255,255,0.3)', fontSize: 14 }}>
                  No transactions found
                </p>
                <p style={{ color: 'rgba(255,255,255,0.2)', fontSize: 12, marginTop: 6 }}>
                  Make a transfer to see history here
                </p>
              </div>
            )}

            {!loading && filtered.map((txn, i) => {
              const credit = isCredit(txn);
              return (
                <div
                  key={txn.id || i}
                  style={{
                    display: 'flex', alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '18px 24px',
                    borderBottom: i < filtered.length - 1
                      ? '1px solid rgba(255,255,255,0.04)' : 'none',
                    transition: 'background 0.15s',
                  }}
                  onMouseEnter={e =>
                    e.currentTarget.style.background = 'rgba(255,255,255,0.02)'}
                  onMouseLeave={e =>
                    e.currentTarget.style.background = 'transparent'}>

                  {/* Icon + Info */}
                  <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                    <div style={{
                      width: 40, height: 40, borderRadius: 12,
                      background: credit
                        ? 'rgba(126,200,126,0.12)'
                        : 'rgba(224,124,124,0.12)',
                      border: `1px solid ${credit
                        ? 'rgba(126,200,126,0.2)'
                        : 'rgba(224,124,124,0.2)'}`,
                      display: 'flex', alignItems: 'center',
                      justifyContent: 'center', fontSize: 16,
                    }}>
                      {credit ? '⬇' : '⬆'}
                    </div>
                    <div>
                      <p style={{ color: '#fff', fontWeight: 500, fontSize: 14 }}>
                        {credit ? 'Received from' : 'Sent to'}{' '}
                        <span style={{ fontFamily: 'monospace', fontSize: 12, color: 'rgba(255,255,255,0.5)' }}>
                          {credit ? txn.fromAccountNumber : txn.toAccountNumber}
                        </span>
                      </p>
                      {txn.description && (
                        <p style={{ color: 'rgba(255,255,255,0.35)', fontSize: 12, marginTop: 2 }}>
                          {txn.description}
                        </p>
                      )}
                      <p style={{ color: 'rgba(255,255,255,0.25)', fontSize: 11, marginTop: 3 }}>
                        {fmtDate(txn.createdAt)}
                      </p>
                    </div>
                  </div>

                  {/* Amount + Status */}
                  <div style={{ textAlign: 'right' }}>
                    <p style={{
                      color: credit ? '#7ec87e' : '#e07c7c',
                      fontWeight: 700, fontSize: 16,
                    }}>
                      {credit ? '+' : '-'}{fmt(txn.amount)}
                    </p>
                    <span style={{
                      display: 'inline-block', marginTop: 4,
                      background: txn.status === 'SUCCESS'
                        ? 'rgba(126,200,126,0.1)' : 'rgba(224,124,124,0.1)',
                      color: txn.status === 'SUCCESS' ? '#7ec87e' : '#e07c7c',
                      border: `1px solid ${txn.status === 'SUCCESS'
                        ? 'rgba(126,200,126,0.2)' : 'rgba(224,124,124,0.2)'}`,
                      fontSize: 10, padding: '2px 8px',
                      borderRadius: 20, fontWeight: 600,
                    }}>
                      {txn.status || 'SUCCESS'}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}