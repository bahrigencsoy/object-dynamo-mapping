terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "6.23.0"
    }
  }
}

provider "aws" {
  region = "us-east-1"
}

# Table 1: game_scores_odm_test
resource "aws_dynamodb_table" "game_scores" {
  name           = "game_scores_odm_test"
  billing_mode   = "PAY_PER_REQUEST"
  table_class    = "STANDARD_INFREQUENT_ACCESS"
  hash_key       = "user_id"
  range_key      = "game_title"

  attribute {
    name = "user_id"
    type = "S"
  }

  attribute {
    name = "game_title"
    type = "S"
  }

  /* DID NOT WORK CHECK LATER: One or more parameter values were invalid: Table KeySchema does not have a range key, which is required when specifying a LocalSecondaryIndex
  local_secondary_index {
    name            = "game_genres_idx"
    range_key       = "game_genre"
    projection_type = "ALL"
  }
   */

  on_demand_throughput {
    max_read_request_units  = 2
    max_write_request_units = 2
  }
}

# Table 2: cached_resource_odm_test
resource "aws_dynamodb_table" "cached_resource" {
  name           = "cached_resource_odm_test"
  billing_mode   = "PAY_PER_REQUEST"
  table_class    = "STANDARD_INFREQUENT_ACCESS"
  hash_key       = "cache_item_key"

  attribute {
    name = "cache_item_key"
    type = "S"
  }

  attribute {
    name = "cached_item_unique_id"
    type = "S"
  }

  global_secondary_index {
    name            = "cache_id_idx"
    hash_key        = "cached_item_unique_id"
    projection_type = "ALL"

    on_demand_throughput {
      max_read_request_units  = 2
      max_write_request_units = 2
    }
  }

  on_demand_throughput {
    max_read_request_units  = 2
    max_write_request_units = 2
  }
}
