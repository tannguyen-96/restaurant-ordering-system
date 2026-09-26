import React from 'react'
import { useApp } from '../context/AppContext'
import { Col, Container, Row } from 'react-bootstrap'
import QRCode from 'react-qr-code'
export default function Home() {
  const { siteName } = useApp()
  const domain = 'https://restaurant-ordering-system-ten-rho.vercel.app/'
  return (
    <>
      <Container>
        <Row>
          <Col className="text-center">
            Home Page {siteName}
          </Col>
        </Row>
        <Row className="mt-3">
          <Col lg={{ span: 4, offset: 4 }}>
            <QRCode
              className="mx-auto d-block"
              value={domain}
              viewBox={`0 0 256 256`}
            />
          </Col>
        </Row>
      </Container>
    </>
  )
}
