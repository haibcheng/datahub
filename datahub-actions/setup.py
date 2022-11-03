import setuptools

package_metadata: dict = {}
with open("src/wap_actions/__init__.py") as fp:
    exec(fp.read(), package_metadata)

setuptools.setup(
    name=package_metadata["__package_name__"],
    version=package_metadata["__version__"],
    zip_safe=False,
    python_requires=">=3.6",
    package_dir={"": "src"},
    packages=setuptools.find_namespace_packages(where="./src"),
    # if you don't already have DataHub Actions installed, add it under install_requires
    install_requires=["acryl-datahub-actions"]
)
