# Project Vision

## Overview
Hedvig Android is a production mobile application that provides insurance management for Hedvig customers on the Android platform.

## Current State
- **Age**: 7 years (since 2019)
- **Status**: Active development (v14.0.10, 13,655 commits)
- **Users**: Hedvig insurance customers in Nordic markets
- **Tech Stack**: Kotlin 2.3.10, Jetpack Compose, Apollo GraphQL, Molecule MVI, Koin

## Purpose
Hedvig Android enables customers to manage their insurance lifecycle entirely from their phone — from purchasing policies and viewing coverage details, to filing claims and managing payments. The app serves as the primary touchpoint between Hedvig and its mobile users.

## Goals (Next 6-12 Months)
- **Feature Development**: Continue expanding functionality with new insurance products and user-facing features
- **Quality & Stability**: Improve app stability, performance, and overall code quality
- **KMP Migration**: Continue migrating modules to Kotlin Multiplatform (currently 43/80+ modules, ~54%)

## Evolution
The project has evolved from its initial architecture to a highly modular monorepo with 80+ modules, adopting modern patterns like Molecule for reactive state management, 100% Jetpack Compose UI (no XML), and progressive KMP adoption. The architecture enforces strict module boundaries — feature modules cannot depend on other feature modules — ensuring clean separation of concerns at scale.
