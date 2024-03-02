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

        runtimePython = platform: nixpkgs-sppl.legacyPackages.${platform}.python39.withPackages (p: [ sppl ]);

        runtimeDeps = platform: let
          pkgs = nixpkgs.legacyPackages.${platform};
          python = runtimePython platform;
        in with pkgs; [
            clj-kondo
            clojure
            git
            openjdk11
            python
        ];

        devShell = let
          python  = runtimePython system;
        in pkgs.mkShell {
          buildInputs = runtimeDeps "${system}";

          shellHook = "export PYTHONPATH=${python}/${python.sitePackages}";
        };

        ociImg = pkgs.dockerTools.buildImage {
          name = "inferenceql.gpm.sppl";
          copyToRoot = runtimeDeps "x86_64-linux";
          # config = {
          #   Cmd = [ "${ociBin}/bin/${pname}" ];
          # };
        };
      in {
        inherit devShell;

        packages = {
          inherit ociImg;
        };
      }
    );
}
