package portals.vldb2023demo.shoppingcart

object ShoppingCartConfig:
  inline val N_EVENTS = 1024 * 1024 * 1024
  inline val N_ATOM_SIZE = 5
  // inline val RATE_LIMIT = 64
  inline val RATE_LIMIT = 4
  inline val N_USERS = 128
  inline val N_ITEMS = 1024
  // inline val LOGGING = false
  inline val LOGGING = true
