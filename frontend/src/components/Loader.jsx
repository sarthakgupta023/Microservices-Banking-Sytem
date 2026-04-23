export default function Loader({ text = 'Loading...' }) {
    return (
      <div style={{
        display: 'flex', flexDirection: 'column',
        alignItems: 'center', justifyContent: 'center',
        minHeight: '100vh', gap: '16px'
      }}>
        <div style={{
          width: 44, height: 44, borderRadius: '50%',
          border: '3px solid rgba(201,168,76,0.15)',
          borderTop: '3px solid #c9a84c',
          animation: 'spin 0.8s linear infinite'
        }}/>
        <p style={{ color: 'rgba(255,255,255,0.4)', fontSize: 13 }}>{text}</p>
        <style>{`@keyframes spin{to{transform:rotate(360deg)}}`}</style>
      </div>
    );
  }