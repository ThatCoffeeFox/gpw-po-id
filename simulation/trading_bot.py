#!/usr/bin/env python3
import requests
import time
import random
import logging
import argparse
from datetime import datetime, timedelta, timezone
import sys

class TradingBot:
    def __init__(self, args):
        self.base_url = args.base_url
        self.auth = (args.username, args.password)
        self.wallet_id = args.wallet_id
        self.company_id = args.company_id
        self.rate_limit = args.rate_limit
        self.base_price = args.base_price
        self.max_order_size = args.max_order_size
        self.order_expiration_sec = args.order_expiration_sec
        self.price_variation = args.price_variation
        self.max_retries = args.max_retries
        self.logger = self.setup_logger()
        
    def setup_logger(self):
        logger = logging.getLogger(f"Bot_{self.wallet_id}_{self.company_id}")
        logger.setLevel(logging.INFO)
        
        ch = logging.StreamHandler(sys.stdout)
        ch.setLevel(logging.INFO)
        
        formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
        ch.setFormatter(formatter)
        
        logger.addHandler(ch)
        return logger

    def run(self):
        self.logger.info("Starting trading bot")
        self.logger.info(f"Configuration: {vars(self)}")
        while True:
            try:
                self.trading_cycle()
            except Exception as e:
                self.logger.error(f"Unexpected error: {str(e)}", exc_info=True)
            time.sleep(self.rate_limit)

    def trading_cycle(self):
        # First cancel any existing active orders
        self.cancel_all_active_orders()

        # Get current market state
        wallet_state = self.get_wallet_state()
        if not wallet_state:
            return
            
        last_price = self.get_last_transaction_price()
        self.log_wallet_state(wallet_state)
        self.logger.info(f"Last transaction price: {last_price}")

        # Determine possible actions
        possible_actions = []
        if float(wallet_state['unlocked_founds']) >= last_price:
            possible_actions.append('BUY')
        if int(wallet_state['unblockedShareAmount']) > 0:
            possible_actions.append('SELL')

        if not possible_actions:
            self.logger.warning("No trading possible - insufficient funds and shares")
            time.sleep(self.rate_limit * 2)  # Wait longer before retrying
            return

        # Randomly choose action
        action = random.choice(possible_actions)
        self.logger.info(f"Selected action: {action}")

        # Generate initial order parameters
        if action == 'BUY':
            price_variation = random.uniform(-self.price_variation, 0)  # Buy at or below market
        else:
            price_variation = random.uniform(0, self.price_variation)  # Sell at or above market
            
        target_price = max(0.01, round(last_price * (1 + price_variation), 2))
        
        # Calculate max size
        if action == 'BUY':
            available_funds = float(wallet_state['unlocked_founds'])
            max_size = min(
                int(available_funds // target_price),
                self.max_order_size
            )
        else:  # SELL
            available_shares = int(wallet_state['unblockedShareAmount'])
            max_size = min(available_shares, self.max_order_size)
            
        if max_size < 1:
            self.logger.warning(f"Not enough resources for {action} order")
            return
            
        size = random.randint(1, max_size)
        
        # Prepare order
        expiration = (datetime.now(timezone.utc) + timedelta(seconds=self.order_expiration_sec)).strftime('%Y-%m-%dT%H:%M:%S')
        order_params = {
            'orderType': action.lower(),
            'sharePrice': target_price,
            'sharesAmount': size,
            'walletId': self.wallet_id,
            'companyId': self.company_id,
            'orderExpirationDate': expiration
        }

        # Place initial order
        self.logger.info(f"Placing {action} order for {size} shares at {target_price}")
        if not self.place_order(order_params):
            self.logger.error("Failed to place initial order - starting new cycle")
            return

        # Check order status with retries
        for attempt in range(self.max_retries):
            time.sleep(self.rate_limit)  # Wait before checking
            
            if self.get_last_transaction_price() != last_price:
                self.logger.info("ORDER WAS MATCHED")
                return
                
            self.logger.info(f"Order still active after {attempt + 1} checks")
            
            # Adjust price for next attempt
            active_orders = self.get_active_orders()

            if action == 'BUY':
                new_price = float(active_orders[0]['sharePrice']) * 1.015  # Increase buy price
            else:
                new_price = float(active_orders[0]['sharePrice']) * 0.985 # Decrease sell price
                
            # # Cancel old order
            self.cancel_order(active_orders[0]['orderId'])
            
            # Create new order with adjusted price
            new_order = {
                'orderType': action.lower(),
                'sharePrice': round(new_price, 2),
                'sharesAmount': size,
                'walletId': self.wallet_id,
                'companyId': self.company_id,
                'orderExpirationDate': expiration
            }
            
            self.logger.info(f"Placing adjusted {action} order at {new_price}")
            if not self.place_order(new_order):
                self.logger.error("Failed to place adjusted order - starting new cycle")
                return

        self.logger.info(f"Max retries ({self.max_retries}) reached - starting new cycle")

    def place_order(self, order_params):
        url = f"{self.base_url}/api/order"
        try:
            response = requests.post(
                url, 
                json=order_params, 
                auth=self.auth, 
                timeout=5
            )
            
            if response.status_code == 200:
                # Empty response is acceptable if status is 200
                self.logger.info("Order placed successfully")
                return True
            else:
                self.logger.error(f"Order placement failed: {response.status_code} - {response.text}")
                return False
        except requests.exceptions.RequestException as e:
            self.logger.error(f"Order request failed: {str(e)}")
            return False
        
    def cancel_order(self, order_id):
        url = f"{self.base_url}/api/order/{order_id}"
        try:
            response = requests.delete(url, auth=self.auth, timeout=5)
            if response.status_code == 200:
                self.logger.info(f"Successfully canceled order {order_id}")
                return True
            else:
                self.logger.error(f"Failed to cancel order {order_id}: {response.status_code} - {response.text}")
        except Exception as e:
            self.logger.error(f"Cancel order request failed for {order_id}: {str(e)}")
        return False

    def cancel_all_active_orders(self):
        url = f"{self.base_url}/api/wallets/{self.wallet_id}/{self.company_id}/active_orders"
        try:
            response = requests.delete(url, auth=self.auth, timeout=5)
            if response.status_code == 200:
                self.logger.info("Successfully cancelled all active orders for wallet and company.")
                return True
            else:
                self.logger.error(f"Failed to cancel active orders: {response.status_code} - {response.text}")
        except Exception as e:
            self.logger.error(f"Cancel all active orders request failed: {str(e)}")
        return False


    # def get_active_orders(self):
    #     url = f"{self.base_url}/api/wallets/{self.wallet_id}/{self.company_id}/active_orders"
    #     try:
    #         response = requests.get(url, auth=self.auth, timeout=5)
    #         if response.status_code == 200:
    #             return response.json()
    #         else:
    #             self.logger.error(f"Active orders API error: {response.status_code} - {response.text}")
    #     except Exception as e:
    #         self.logger.error(f"Active orders request failed: {str(e)}")
    #     return []

    def get_active_orders(self):
        url = f"{self.base_url}/api/wallets/{self.wallet_id}/{self.company_id}/active_orders"
        try:
            response = requests.get(url, auth=self.auth, timeout=5)
            if response.status_code == 200:
                orders = response.json()
                if orders:
                    self.logger.info(f"Active orders ({len(orders)}):")
                    for order in orders:
                        self.logger.info(
                            f" - ID: {order['orderId']}, "
                            f"Type: {order['orderType']}, "
                            f"Price: {order['sharePrice']}, "
                            f"Amount: {order['sharesAmount']}, "
                            f"Expires: {order['orderExpirationDate']}"
                        )
                else:
                    self.logger.info("No active orders.")
                return orders
            else:
                self.logger.error(f"Active orders API error: {response.status_code} - {response.text}")
        except Exception as e:
            self.logger.error(f"Active orders request failed: {str(e)}")
        return []


    def get_wallet_state(self):
        url = f"{self.base_url}/api/wallets/{self.wallet_id}/{self.company_id}"
        try:
            response = requests.get(url, auth=self.auth, timeout=5)
            if response.status_code == 200:
                return response.json()
            else:
                self.logger.error(f"Wallet state API error: {response.status_code} - {response.text}")
        except Exception as e:
            self.logger.error(f"Wallet state request failed: {str(e)}")
        return None

    def get_last_transaction_price(self):
        url = f"{self.base_url}/api/companies/{self.company_id}/transactions?limit=1"
        try:
            response = requests.get(url, auth=self.auth, timeout=5)
            if response.status_code == 200:
                transactions = response.json()
                if transactions:
                    return float(transactions[0]['sharePrice'])
            else:
                self.logger.error(f"Transactions API error: {response.status_code} - {response.text}")
        except Exception as e:
            self.logger.error(f"Transactions request failed: {str(e)}")
        return self.base_price

    def log_wallet_state(self, wallet_state):
        try:
            self.logger.info(f"Wallet state: "
                           f"Funds: {wallet_state['founds']} | "
                           f"Unlocked: {wallet_state['unlocked_founds']} | "
                           f"Shares: {wallet_state['shareAmount']} | "
                           f"Unblocked: {wallet_state['unblockedShareAmount']}")
        except KeyError as e:
            self.logger.error(f"Missing key in wallet state: {str(e)}")

def parse_args():
    parser = argparse.ArgumentParser(description='Stock Trading Bot')
    
    # Required arguments
    parser.add_argument('--username', required=True, help='Authentication username')
    parser.add_argument('--password', required=True, help='Authentication password')
    parser.add_argument('--wallet_id', type=int, required=True, help='Wallet ID')
    parser.add_argument('--company_id', type=int, required=True, help='Company ID')
    
    # Optional arguments
    parser.add_argument('--base_url', default='http://localhost:8080', 
                       help='Base API URL (default: http://localhost:8080)')
    parser.add_argument('--rate_limit', type=float, default=1.0, 
                       help='Seconds between trading cycles (default: 1)')
    parser.add_argument('--base_price', type=float, default=100.0, 
                       help='Fallback base price when no transactions exist (default: 100)')
    parser.add_argument('--max_order_size', type=int, default=5, 
                       help='Max shares per order (default: 5)')
    parser.add_argument('--order_expiration_sec', type=float, default=60.0, 
                       help='Order expiration time in seconds (default: 60)')
    parser.add_argument('--price_variation', type=float, default=0.05, 
                       help='Price variation percentage (default: 0.05 = 5%%)')
    parser.add_argument('--max_retries', type=int, default=4,
                       help='Max retries for order placement (default: 4')
    
    return parser.parse_args()

if __name__ == "__main__":
    args = parse_args()
    bot = TradingBot(args)
    bot.run()