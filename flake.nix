{
  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixpkgs-unstable";
    nixpkgs-sppl.url = "github:nixos/nixpkgs/35a74aa665a681b60d68e3d3400fcaa11690ee50";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, nixpkgs-sppl, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};

        pypkgs = nixpkgs-sppl.legacyPackages.${system}.python39Packages;

        sppl = pypkgs.buildPythonPackage rec { # not in nixpkgs
          pname = "sppl";
          version = "2.0.4";

          src = pypkgs.fetchPypi {
            inherit pname version;
            sha256 = "sha256-QAp77L8RpN86V4O8F1zNA8O/szm9hNa4wWFT13av6BE=";
          };

          propagatedBuildInputs = with pypkgs; [
            astunparse
            numpy
            scipy
            sympy
          ];

          checkInputs = with pypkgs; [
            coverage
            pytest
            pytestCheckHook
            pytest-timeout
          ];

          pytestFlagsArray = [ "--pyargs" "sppl" ];

          pipInstallFlags = [ "--no-deps" ];
        };

        python = nixpkgs-sppl.legacyPackages.${system}.python39.withPackages (p: [ sppl ]);
      in {
        devShell = pkgs.mkShell {
          buildInputs = with pkgs; [
            clj-kondo
            clojure
            git
            openjdk11
            python
          ];

          shellHook = "export PYTHONPATH=${python}/${python.sitePackages}";
        };
      }
    );
}
