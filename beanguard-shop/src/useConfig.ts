import { useContext } from 'react'
import { ConfigContext } from './configContext'

export const useConfig = () => useContext(ConfigContext)
