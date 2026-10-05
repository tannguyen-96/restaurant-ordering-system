import { Form, Button, Container, Card, Alert, Spinner } from 'react-bootstrap'
import { useState } from 'react'
import axios from '../api/axios'

const Login = () => {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    console.log('Sign in ...')
    try {
      setSubmitting(true)
      const response = await axios.post('/auth/login',
        JSON.stringify({ username, password }),
        {
          headers: { 'Content-Type': 'application/json' },
          withCredentials: true
        }
      );
      console.log('Login response:', response)
    } catch (err) {
      console.error('Login error:', err)
      setError('Failed to sign in. Please check your credentials and try again.')
      // if (!err?.response) {
      //           setErrMsg('No Server Response');
      //       } else if (err.response?.status === 400) {
      //           setErrMsg('Missing Username or Password');
      //       } else if (err.response?.status === 401) {
      //           setErrMsg('Unauthorized');
      //       } else {
      //           setErrMsg('Login Failed');
      //       }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
        <Card style={{ width: '24rem' }}>
          <Card.Body>
            <Card.Title className="mb-3">Login</Card.Title>
            {error && <Alert variant="danger">{error}</Alert>}
            <Form onSubmit={handleSubmit}>
              <Form.Group className="mb-3" controlId="username">
                <Form.Label>Username</Form.Label>
                <Form.Control
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                />
              </Form.Group>
              <Form.Group className="mb-3" controlId="password">
                <Form.Label>Password</Form.Label>
                <Form.Control
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </Form.Group>
              <Button type="submit" className="w-100" disabled={submitting}>
                {submitting ? <Spinner animation="border" size="sm" /> : 'Sign In'}
              </Button>
            </Form>
          </Card.Body>
        </Card>
      </Container>
    </>
  )
}

export default Login